import http from 'k6/http';
import { check, group, sleep } from 'k6';
import exec from 'k6/execution';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const PROFILE = (__ENV.PROFILE || 'smoke').toLowerCase();
const MAX_VUS = Number(__ENV.MAX_VUS || 25);
const P95_MS = Number(__ENV.P95_MS || 1000);
const THINK_TIME = Number(__ENV.THINK_TIME || 0.2);

const EXPECTED_CONFLICT = http.expectedStatuses(409);

const PRICE_CASES = [
    { weight: 0.5, expectedPrice: 12 },
    { weight: 3, expectedPrice: 17 },
    { weight: 7, expectedPrice: 25 },
    { weight: 15, expectedPrice: 35 },
];

const smokeScenario = {
    executor: 'shared-iterations',
    vus: 1,
    iterations: 1,
    maxDuration: '3m',
};

const loadScenario = {
    executor: 'ramping-vus',
    startVUs: 1,
    gracefulRampDown: '30s',
    stages: [
        { duration: '15s', target: Math.max(1, Math.ceil(MAX_VUS * 0.2)) },
        { duration: '20s', target: Math.max(1, Math.ceil(MAX_VUS * 0.5)) },
        { duration: '20s', target: MAX_VUS },
        { duration: '20s', target: MAX_VUS },
        { duration: '15s', target: 0 },
    ],
};

export const options = {
    scenarios: {
        full_logistics_flow: PROFILE === 'load' ? loadScenario : smokeScenario,
    },
    thresholds: {
        checks: ['rate>0.99'],
        http_req_failed: ['rate<0.01'],
        http_req_duration: [`p(95)<${P95_MS}`],
        'http_req_duration{entity:customer}': [`p(95)<${P95_MS}`],
        'http_req_duration{entity:shipment}': [`p(95)<${P95_MS}`],
        'http_req_duration{entity:courier}': [`p(95)<${P95_MS}`],
        'http_req_duration{entity:vehicle}': [`p(95)<${P95_MS}`],
    },
};

function requestParams(entity, action, name, responseCallback = null) {
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
        tags: {
            entity,
            action,
            name,
        },
    };

    if (responseCallback !== null) {
        params.responseCallback = responseCallback;
    }

    return params;
}

function parseJson(response) {
    try {
        return response.json();
    } catch (_) {
        return null;
    }
}

function shortBody(response) {
    const body = response.body === null || response.body === undefined
        ? ''
        : String(response.body);

    return body.length > 500 ? `${body.slice(0, 500)}...` : body;
}

function expectJson(response, expectedStatus, label, validator = null) {
    const body = parseJson(response);

    const checks = {
        [`${label}: status is ${expectedStatus}`]: (r) => r.status === expectedStatus,
        [`${label}: response is JSON`]: () => body !== null,
    };

    if (validator !== null) {
        checks[`${label}: response body is valid`] = () =>
            body !== null && validator(body);
    }

    const ok = check(response, checks);

    if (!ok) {
        console.error(
            `${label} failed. status=${response.status}, body=${shortBody(response)}`
        );
    }

    return ok ? body : null;
}

function expectStatus(response, expectedStatus, label) {
    const ok = check(response, {
        [`${label}: status is ${expectedStatus}`]: (r) => r.status === expectedStatus,
    });

    if (!ok) {
        console.error(
            `${label} failed. status=${response.status}, body=${shortBody(response)}`
        );
    }

    return ok;
}

function expectConflict(response, label) {
    return expectStatus(response, 409, label);
}

function uniqueToken() {
    return `${Date.now()}-${exec.vu.idInTest}-${exec.scenario.iterationInTest}`;
}

function uniquePhone(unique, salt = 0) {
    const digits = `${Date.now()}${exec.vu.idInTest}${exec.scenario.iterationInTest}${salt}`
        .replace(/\D/g, '')
        .slice(-12)
        .padStart(12, '0');

    return `+48${digits}`;
}

function buildCustomerPayload(unique, prefix = 'main') {
    return {
        firstName: `Load${prefix}`,
        lastName: 'Tester',
        email: `${prefix}-${unique}@example.com`,
        phoneNumber: uniquePhone(unique, prefix.length),
        address: {
            street: 'Testowa',
            buildingNumber: '10',
            apartmentNumber: '2',
            city: 'Gdansk',
            postalCode: '80-001',
            country: 'Poland',
        },
    };
}

function createCustomer(unique, prefix = 'main') {
    const payload = buildCustomerPayload(unique, prefix);

    const response = http.post(
        `${BASE_URL}/api/customers`,
        JSON.stringify(payload),
        requestParams('customer', 'create', 'POST /api/customers')
    );

    const body = expectJson(response, 201, `customer create (${prefix})`, (json) =>
        json.id !== null &&
        json.id !== undefined &&
        json.email === payload.email.toLowerCase() &&
        json.active === true &&
        json.address !== null &&
        json.address.city === 'Gdansk'
    );

    return body === null ? null : { body, payload };
}

function customerFlow(unique) {
    let customer = null;

    group('Customer flow', () => {
        const created = createCustomer(unique, 'main');
        if (created === null) {
            return;
        }

        customer = created.body;
        const customerId = customer.id;

        if (PROFILE === 'smoke') {
            const duplicateResponse = http.post(
                `${BASE_URL}/api/customers`,
                JSON.stringify(created.payload),
                requestParams(
                    'customer',
                    'duplicate-email',
                    'POST /api/customers [duplicate]',
                    EXPECTED_CONFLICT
                )
            );
            expectConflict(duplicateResponse, 'customer duplicate email');
        }

        const getResponse = http.get(
            `${BASE_URL}/api/customers/${customerId}`,
            requestParams('customer', 'get-by-id', 'GET /api/customers/:id')
        );
        expectJson(getResponse, 200, 'customer get by id', (json) =>
            json.id === customerId && json.email === created.payload.email.toLowerCase()
        );

        const updatedFirstName = `Updated${exec.vu.idInTest}`;
        const updateResponse = http.patch(
            `${BASE_URL}/api/customers/${customerId}`,
            JSON.stringify({
                firstName: updatedFirstName,
                phoneNumber: uniquePhone(unique, 9),
            }),
            requestParams('customer', 'update', 'PATCH /api/customers/:id')
        );
        expectJson(updateResponse, 200, 'customer update', (json) =>
            json.id === customerId && json.firstName === updatedFirstName
        );

        const deactivateResponse = http.patch(
            `${BASE_URL}/api/customers/${customerId}/deactivate`,
            null,
            requestParams('customer', 'deactivate', 'PATCH /api/customers/:id/deactivate')
        );
        expectJson(deactivateResponse, 200, 'customer deactivate', (json) =>
            json.id === customerId && json.active === false
        );

        const activateResponse = http.patch(
            `${BASE_URL}/api/customers/${customerId}/activate`,
            null,
            requestParams('customer', 'activate', 'PATCH /api/customers/:id/activate')
        );
        expectJson(activateResponse, 200, 'customer activate', (json) =>
            json.id === customerId && json.active === true
        );

        if (PROFILE === 'smoke') {
            const allResponse = http.get(
                `${BASE_URL}/api/customers`,
                requestParams('customer', 'find-all', 'GET /api/customers')
            );
            expectJson(allResponse, 200, 'customer find all', (json) => Array.isArray(json));
        }
    });

    return customer;
}

function customerDeleteBehavior(unique) {
    if (PROFILE !== 'smoke') {
        return;
    }

    group('Customer delete behavior', () => {
        const created = createCustomer(unique, 'delete');
        if (created === null) {
            return;
        }

        const customerId = created.body.id;

        const deleteActiveResponse = http.del(
            `${BASE_URL}/api/customers/${customerId}`,
            null,
            requestParams(
                'customer',
                'delete-active-conflict',
                'DELETE /api/customers/:id [active]',
                EXPECTED_CONFLICT
            )
        );
        expectConflict(deleteActiveResponse, 'customer cannot delete active');

        const deactivateResponse = http.patch(
            `${BASE_URL}/api/customers/${customerId}/deactivate`,
            null,
            requestParams('customer', 'deactivate-for-delete', 'PATCH /api/customers/:id/deactivate')
        );
        const deactivated = expectJson(
            deactivateResponse,
            200,
            'customer deactivate before delete',
            (json) => json.id === customerId && json.active === false
        );

        if (deactivated === null) {
            return;
        }

        const deleteResponse = http.del(
            `${BASE_URL}/api/customers/${customerId}`,
            null,
            requestParams('customer', 'delete', 'DELETE /api/customers/:id')
        );
        expectStatus(deleteResponse, 204, 'customer delete inactive');
    });
}

function createShipment(customerId, unique, suffix, weight, expectedPrice) {
    const payload = {
        customerId,
        pickupAddress: {
            street: `Odbiorowa-${suffix}`,
            buildingNumber: '10',
            apartmentNumber: '2',
            city: 'Gdansk',
            postalCode: '80-100',
            country: 'Poland',
        },
        deliveryAddress: {
            street: `Wysylkowa-${suffix}`,
            buildingNumber: '25',
            apartmentNumber: '4',
            city: 'Warszawa',
            postalCode: '00-001',
            country: 'Poland',
        },
        weight,
    };

    const response = http.post(
        `${BASE_URL}/api/shipments`,
        JSON.stringify(payload),
        requestParams('shipment', 'create', 'POST /api/shipments')
    );

    return expectJson(response, 201, `shipment create (${suffix})`, (json) =>
        json.id !== null &&
        json.id !== undefined &&
        json.customerId === customerId &&
        json.status === 'CREATED' &&
        Number(json.weight) === Number(weight) &&
        Number(json.price) === Number(expectedPrice) &&
        json.pickupAddress !== null &&
        json.pickupAddress.city === 'Gdansk' &&
        json.deliveryAddress !== null &&
        json.deliveryAddress.city === 'Warszawa'
    );
}

function patchShipmentStatus(shipmentId, endpoint, expectedStatus, action) {
    const response = http.patch(
        `${BASE_URL}/api/shipments/${shipmentId}/${endpoint}`,
        null,
        requestParams(
            'shipment',
            action,
            `PATCH /api/shipments/:id/${endpoint}`
        )
    );

    return expectJson(response, 200, `shipment ${action}`, (json) =>
        json.id === shipmentId && json.status === expectedStatus
    );
}

function shipmentFlow(unique, customerId) {
    if (customerId === null || customerId === undefined) {
        console.error('Shipment flow skipped: customer was not created.');
        return;
    }

    group('Shipment flow', () => {
        if (PROFILE === 'smoke') {
            const shipments = [];

            for (let i = 0; i < PRICE_CASES.length; i += 1) {
                const priceCase = PRICE_CASES[i];
                shipments.push(
                    createShipment(
                        customerId,
                        unique,
                        `price-${i}`,
                        priceCase.weight,
                        priceCase.expectedPrice
                    )
                );
            }

            const deliveredShipment = shipments[2];
            if (deliveredShipment !== null) {
                const shipmentId = deliveredShipment.id;

                const getResponse = http.get(
                    `${BASE_URL}/api/shipments/${shipmentId}`,
                    requestParams('shipment', 'get-by-id', 'GET /api/shipments/:id')
                );
                expectJson(getResponse, 200, 'shipment get by id', (json) =>
                    json.id === shipmentId && json.status === 'CREATED'
                );

                const invalidTransitResponse = http.patch(
                    `${BASE_URL}/api/shipments/${shipmentId}/in-transit`,
                    null,
                    requestParams(
                        'shipment',
                        'invalid-created-to-in-transit',
                        'PATCH /api/shipments/:id/in-transit [invalid]',
                        EXPECTED_CONFLICT
                    )
                );
                expectConflict(
                    invalidTransitResponse,
                    'shipment CREATED cannot go directly to IN_TRANSIT'
                );

                patchShipmentStatus(
                    shipmentId,
                    'ready-for-pickup',
                    'READY_FOR_PICKUP',
                    'ready-for-pickup'
                );
                patchShipmentStatus(
                    shipmentId,
                    'in-transit',
                    'IN_TRANSIT',
                    'in-transit'
                );
                patchShipmentStatus(
                    shipmentId,
                    'delivered',
                    'DELIVERED',
                    'delivered'
                );

                const invalidCancelResponse = http.patch(
                    `${BASE_URL}/api/shipments/${shipmentId}/cancelled`,
                    null,
                    requestParams(
                        'shipment',
                        'invalid-delivered-to-cancelled',
                        'PATCH /api/shipments/:id/cancelled [invalid]',
                        EXPECTED_CONFLICT
                    )
                );
                expectConflict(
                    invalidCancelResponse,
                    'shipment DELIVERED cannot be cancelled'
                );
            }

            const cancelledShipment = shipments[1];
            if (cancelledShipment !== null) {
                patchShipmentStatus(
                    cancelledShipment.id,
                    'cancelled',
                    'CANCELLED',
                    'cancelled'
                );
            }

            const allResponse = http.get(
                `${BASE_URL}/api/shipments`,
                requestParams('shipment', 'find-all', 'GET /api/shipments')
            );
            expectJson(allResponse, 200, 'shipment find all', (json) => Array.isArray(json));

            return;
        }

        const caseIndex = Number(exec.scenario.iterationInTest) % PRICE_CASES.length;
        const priceCase = PRICE_CASES[caseIndex];
        const shipment = createShipment(
            customerId,
            unique,
            `load-${caseIndex}`,
            priceCase.weight,
            priceCase.expectedPrice
        );

        if (shipment === null) {
            return;
        }

        const shipmentId = shipment.id;

        const getResponse = http.get(
            `${BASE_URL}/api/shipments/${shipmentId}`,
            requestParams('shipment', 'get-by-id', 'GET /api/shipments/:id')
        );
        expectJson(getResponse, 200, 'shipment get by id', (json) =>
            json.id === shipmentId && json.status === 'CREATED'
        );

        if (Number(exec.scenario.iterationInTest) % 2 === 0) {
            patchShipmentStatus(
                shipmentId,
                'ready-for-pickup',
                'READY_FOR_PICKUP',
                'ready-for-pickup'
            );
            patchShipmentStatus(shipmentId, 'in-transit', 'IN_TRANSIT', 'in-transit');
            patchShipmentStatus(shipmentId, 'delivered', 'DELIVERED', 'delivered');
        } else {
            patchShipmentStatus(shipmentId, 'cancelled', 'CANCELLED', 'cancelled');
        }
    });
}

function buildCourierPayload(unique) {
    return {
        firstName: 'Load',
        lastName: 'Courier',
        phoneNumber: uniquePhone(unique, 77),
    };
}

function courierFlow(unique) {
    group('Courier flow', () => {
        const payload = buildCourierPayload(unique);

        const createResponse = http.post(
            `${BASE_URL}/api/couriers`,
            JSON.stringify(payload),
            requestParams('courier', 'create', 'POST /api/couriers')
        );

        const courier = expectJson(createResponse, 201, 'courier create', (json) =>
            json.id !== null &&
            json.id !== undefined &&
            json.phoneNumber === payload.phoneNumber &&
            json.active === true &&
            json.available === true
        );

        if (courier === null) {
            return;
        }

        const courierId = courier.id;

        if (PROFILE === 'smoke') {
            const duplicateResponse = http.post(
                `${BASE_URL}/api/couriers`,
                JSON.stringify(payload),
                requestParams(
                    'courier',
                    'duplicate-phone',
                    'POST /api/couriers [duplicate]',
                    EXPECTED_CONFLICT
                )
            );
            expectConflict(duplicateResponse, 'courier duplicate phone number');
        }

        const getResponse = http.get(
            `${BASE_URL}/api/couriers/${courierId}`,
            requestParams('courier', 'get-by-id', 'GET /api/couriers/:id')
        );
        expectJson(getResponse, 200, 'courier get by id', (json) =>
            json.id === courierId && json.available === true
        );

        if (PROFILE === 'smoke') {
            const invalidAvailableResponse = http.patch(
                `${BASE_URL}/api/couriers/${courierId}/available`,
                null,
                requestParams(
                    'courier',
                    'invalid-already-available',
                    'PATCH /api/couriers/:id/available [invalid]',
                    EXPECTED_CONFLICT
                )
            );
            expectConflict(invalidAvailableResponse, 'courier already available conflict');
        }

        const busyResponse = http.patch(
            `${BASE_URL}/api/couriers/${courierId}/busy`,
            null,
            requestParams('courier', 'busy', 'PATCH /api/couriers/:id/busy')
        );
        expectJson(busyResponse, 200, 'courier mark busy', (json) =>
            json.id === courierId && json.active === true && json.available === false
        );

        if (PROFILE === 'smoke') {
            const invalidDeactivateResponse = http.patch(
                `${BASE_URL}/api/couriers/${courierId}/deactivate`,
                null,
                requestParams(
                    'courier',
                    'invalid-deactivate-busy',
                    'PATCH /api/couriers/:id/deactivate [busy]',
                    EXPECTED_CONFLICT
                )
            );
            expectConflict(invalidDeactivateResponse, 'courier busy cannot deactivate');
        }

        const availableResponse = http.patch(
            `${BASE_URL}/api/couriers/${courierId}/available`,
            null,
            requestParams('courier', 'available', 'PATCH /api/couriers/:id/available')
        );
        expectJson(availableResponse, 200, 'courier mark available', (json) =>
            json.id === courierId && json.active === true && json.available === true
        );

        const deactivateResponse = http.patch(
            `${BASE_URL}/api/couriers/${courierId}/deactivate`,
            null,
            requestParams('courier', 'deactivate', 'PATCH /api/couriers/:id/deactivate')
        );
        expectJson(deactivateResponse, 200, 'courier deactivate', (json) =>
            json.id === courierId && json.active === false && json.available === false
        );

        const activateResponse = http.patch(
            `${BASE_URL}/api/couriers/${courierId}/activate`,
            null,
            requestParams('courier', 'activate', 'PATCH /api/couriers/:id/activate')
        );
        expectJson(activateResponse, 200, 'courier activate', (json) =>
            json.id === courierId && json.active === true && json.available === true
        );

        if (PROFILE === 'smoke') {
            const allResponse = http.get(
                `${BASE_URL}/api/couriers`,
                requestParams('courier', 'find-all', 'GET /api/couriers')
            );
            expectJson(allResponse, 200, 'courier find all', (json) => Array.isArray(json));
        }
    });
}

function buildVehiclePayload(unique) {
    const types = ['CAR', 'VAN', 'TRUCK'];
    const typeIndex = Number(exec.scenario.iterationInTest) % types.length;

    return {
        brand: 'Ford',
        model: typeIndex === 2 ? 'F-Max' : 'Transit',
        registrationNumber: `K6-${exec.vu.idInTest}-${exec.scenario.iterationInTest}-${Date.now()}`,
        vehicleType: types[typeIndex],
        maximumLoad: typeIndex === 2 ? 1600.0 : 120.0,
    };
}

function patchVehicleStatus(vehicleId, status, expectedActive, expectedAvailable) {
    const response = http.patch(
        `${BASE_URL}/api/vehicles/${vehicleId}/status`,
        JSON.stringify({ status }),
        requestParams(
            'vehicle',
            `status-${status.toLowerCase()}`,
            'PATCH /api/vehicles/:id/status'
        )
    );

    return expectJson(response, 200, `vehicle status -> ${status}`, (json) =>
        json.id === vehicleId &&
        json.active === expectedActive &&
        json.available === expectedAvailable
    );
}

function vehicleFlow(unique) {
    group('Vehicle flow', () => {
        const payload = buildVehiclePayload(unique);

        const createResponse = http.post(
            `${BASE_URL}/api/vehicles`,
            JSON.stringify(payload),
            requestParams('vehicle', 'create', 'POST /api/vehicles')
        );

        const vehicle = expectJson(createResponse, 201, 'vehicle create', (json) =>
            json.id !== null &&
            json.id !== undefined &&
            json.registrationNumber === payload.registrationNumber &&
            json.vehicleType === payload.vehicleType &&
            Number(json.maximumLoad) === Number(payload.maximumLoad) &&
            json.active === true &&
            json.available === true
        );

        if (vehicle === null) {
            return;
        }

        const vehicleId = vehicle.id;

        if (PROFILE === 'smoke') {
            const duplicateResponse = http.post(
                `${BASE_URL}/api/vehicles`,
                JSON.stringify(payload),
                requestParams(
                    'vehicle',
                    'duplicate-registration',
                    'POST /api/vehicles [duplicate]',
                    EXPECTED_CONFLICT
                )
            );
            expectConflict(duplicateResponse, 'vehicle duplicate registration number');
        }

        const getResponse = http.get(
            `${BASE_URL}/api/vehicles/${vehicleId}`,
            requestParams('vehicle', 'get-by-id', 'GET /api/vehicles/:id')
        );
        expectJson(getResponse, 200, 'vehicle get by id', (json) =>
            json.id === vehicleId && json.available === true
        );

        if (PROFILE === 'smoke') {
            const alreadyAvailableResponse = http.patch(
                `${BASE_URL}/api/vehicles/${vehicleId}/status`,
                JSON.stringify({ status: 'AVAILABLE' }),
                requestParams(
                    'vehicle',
                    'invalid-already-available',
                    'PATCH /api/vehicles/:id/status [already available]',
                    EXPECTED_CONFLICT
                )
            );
            expectConflict(alreadyAvailableResponse, 'vehicle already available conflict');
        }

        patchVehicleStatus(vehicleId, 'BUSY', true, false);

        if (PROFILE === 'smoke') {
            const busyToInactiveResponse = http.patch(
                `${BASE_URL}/api/vehicles/${vehicleId}/status`,
                JSON.stringify({ status: 'INACTIVE' }),
                requestParams(
                    'vehicle',
                    'invalid-busy-to-inactive',
                    'PATCH /api/vehicles/:id/status [busy to inactive]',
                    EXPECTED_CONFLICT
                )
            );
            expectConflict(busyToInactiveResponse, 'vehicle busy cannot become inactive');
        }

        patchVehicleStatus(vehicleId, 'AVAILABLE', true, true);
        patchVehicleStatus(vehicleId, 'INACTIVE', false, false);
        patchVehicleStatus(vehicleId, 'AVAILABLE', true, true);

        if (PROFILE === 'smoke') {
            const allResponse = http.get(
                `${BASE_URL}/api/vehicles`,
                requestParams('vehicle', 'find-all', 'GET /api/vehicles')
            );
            expectJson(allResponse, 200, 'vehicle find all', (json) => Array.isArray(json));
        }
    });
}

export default function () {
    const unique = uniqueToken();

    const customer = customerFlow(unique);

    if (customer !== null) {
        shipmentFlow(unique, customer.id);
    }

    courierFlow(unique);
    vehicleFlow(unique);
    customerDeleteBehavior(unique);

    sleep(THINK_TIME);
}
