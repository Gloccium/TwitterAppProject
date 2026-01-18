import React from 'react';
import { Routes, Route, Link, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';

function App() {
    const token = localStorage.getItem('jwt');

    return (
        <div style={{ maxWidth: 600, margin: '40px auto', fontFamily: 'sans-serif' }}>
            <nav style={{ marginBottom: 20 }}>
                <Link to="/login" style={{ marginRight: 10 }}>Login</Link>
                <Link to="/register">Register</Link>
            </nav>

            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />

                {/* простая страница после логина */}
                <Route
                    path="/profile"
                    element={
                        token ? (
                            <div>
                                <h2>Logged in</h2>
                                <p>JWT stored in localStorage.</p>
                                <button
                                    onClick={() => {
                                        localStorage.removeItem('jwt');
                                        window.location.href = '/login';
                                    }}
                                >
                                    Logout
                                </button>
                            </div>
                        ) : (
                            <Navigate to="/login" />
                        )
                    }
                />

                <Route path="*" element={<Navigate to="/login" />} />
            </Routes>
        </div>
    );
}

export default App;