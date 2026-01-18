import axios from 'axios';

const UMS_BASE_URL = 'http://localhost:9000';

export async function login(email, password) {
    const res = await axios.post(`${UMS_BASE_URL}/auth/login`, { email, password });
    return res.data; // наш JSON с code/message/data
}

export async function register(name, email, password) {
    const res = await axios.post(`${UMMS_BASE_URL}/auth/register`, { name, email, password });
    return res.data;
}

// GitHub login: просто открываем окно на /auth/github/login
export function redirectToGithubLogin() {
    window.location.href = `${UMS_BASE_URL}/auth/github/login`;
}