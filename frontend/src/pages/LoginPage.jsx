import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login, redirectToGithubLogin } from '../api/authApi.js';

function LoginPage() {
    const [email, setEmail] = useState('angela@merkel.de'); // для теста
    const [password, setPassword] = useState('password');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');
        try {
            const res = await login(email, password);
            if (res.code !== '200') {
                setError(res.message || 'Login failed');
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
            <h2>Login</h2>
            <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
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
                <button type="submit">Login</button>
            </form>

            {error && <p style={{ color: 'red' }}>{error}</p>}

            <hr style={{ margin: '20px 0' }} />

            <button onClick={redirectToGithubLogin}>
                Login with GitHub
            </button>
        </div>
    );
}

export default LoginPage;