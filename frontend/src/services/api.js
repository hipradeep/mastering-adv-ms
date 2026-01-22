import axios from 'axios';

// Service 1: Issue to Patient (Port 8081)
export const issueApi = axios.create({
    baseURL: 'http://10.226.30.45:8081/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

// Service 2: Inventory (Port 8082)
export const inventoryApi = axios.create({
    baseURL: 'http://10.226.30.45:8082/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

// Service 3: Orchestrator (Port 8083)
export const orchestratorApi = axios.create({
    baseURL: 'http://10.226.30.45:8083/api',
    headers: {
        'Content-Type': 'application/json',
    },
});
