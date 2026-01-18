import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { register } from '../api/authApi.js';

function RegisterPage() {
    const [name, setName] = useState('New User');
    const [email, setEmail] = useState('newuser@example.com');
    const [password, setPassword] = useState('password');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleRegister = async (e) => {
        e.preventDefault();
        setError('');
        try {
            const res = await register(name, email, password);
            if (res.code !== '201') {
                setError(res.message || 'Register failed');
                return;
            }
            const token = res.data.token;
            localStorage.setItem('jwt', token);
            navigate('/profile');
        } catch (err) {
            console.error(err);
            setError('Request error');
        }
    };

    return (
        <div>
            <h2>Register</h2>
            <form onSubmit={handleRegister} style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                <input
                    type="text"
                    placeholder="name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    required
                />
                <input
                    type="email"
                    placeholder="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                />
                <input
                    type="password"
                    placeholder="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />
                <button type="submit">Register</button>
            </form>

            {error && <p style={{ color: 'red' }}>{error}</p>}
        </div>
    );
}

export default RegisterPage;