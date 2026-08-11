import http from 'k6/http';
import { check } from 'k6';
import exec from 'k6/execution';

const BASE_URL = 'http://localhost:8080';

export const options = {
    scenarios: {
        create_customers_and_shipments: {
            executor: 'ramping-vus',

            startVUs: 10,

            stages: [
                { duration: '10s', target: 20 },
                { duration: '10s', target: 50 },
                { duration: '10s', target: 100 },
                { duration: '10s', target: 200 },
                { duration: '10s', target: 0 },
            ],
        },
    },
};

const jsonHeaders = {
    headers: {
        'Content-Type': 'application/json',
    },
};

export default function () {

    const unique =
        `${Date.now()}-${exec.vu.idInTest}-${exec.vu.iterationInScenario}`;

    // =========================
    // 1. CREATE CUSTOMER
    // =========================

    const customerPayload = JSON.stringify({
        firstName: `Load${unique}`,
        lastName: 'Tester',
        email: `load-${unique}@example.com`,
        phoneNumber: '+48 500 100 200',

        address: {
            street: 'Testowa',
            buildingNumber: '10',
            apartmentNumber: '2',
            city: 'Gdansk',
            postalCode: '80-001',
            country: 'Poland',
        },
    });

    const customerResponse = http.post(
        `${BASE_URL}/api/customers`,
        customerPayload,
        jsonHeaders
    );

    const customerCreated = check(customerResponse, {
        'customer returned 201': (response) => response.status === 201,
    });

    if (!customerCreated) {
        console.error(
            `Customer creation failed. Status=${customerResponse.status}, body=${customerResponse.body}`
        );
        return;
    }

    const customerId = customerResponse.json().id;

    check(customerId, {
        'customer has id': (id) => id !== null && id !== undefined,
    });


    // =========================
    // 2. CREATE SHIPMENT
    // =========================

    const shipmentPayload = JSON.stringify({
        customerId: customerId,

        pickupAddress: {
            street: 'Odbiorowa',
            buildingNumber: '10',
            apartmentNumber: '2',
            city: 'Gdansk',
            postalCode: '80-100',
            country: 'Poland',
        },

        deliveryAddress: {
            street: 'Wysylkowa',
            buildingNumber: '25',
            apartmentNumber: '4',
            city: 'Warszawa',
            postalCode: '00-001',
            country: 'Poland',
        },

        weight: 7.00,
    });

    const shipmentResponse = http.post(
        `${BASE_URL}/api/shipments`,
        shipmentPayload,
        jsonHeaders
    );

    check(shipmentResponse, {
        'shipment returned 201': (response) => response.status === 201,
        'shipment belongs to customer': (response) =>
            response.status === 201 &&
            response.json().customer.id === customerId,
        'shipment status is CREATED': (response) =>
            response.status === 201 &&
            response.json().status === 'CREATED',
    });

    if (shipmentResponse.status !== 201) {
        console.error(
            `Shipment creation failed for customer ${customerId}. ` +
            `Status=${shipmentResponse.status}, body=${shipmentResponse.body}`
        );
    }
}
