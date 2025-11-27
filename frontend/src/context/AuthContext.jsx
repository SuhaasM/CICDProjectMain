import { useState, useEffect } from 'react';
import axios from 'axios';
import { AuthContext } from './context.js';

export const AuthProvider = ({ children }) => {
    // Initialize state from localStorage to stay logged in
    const [user, setUser] = useState(JSON.parse(localStorage.getItem('user')));
    const [token, setToken] = useState(localStorage.getItem('token'));
    
    // Set the authorization header when the component mounts or token changes
    useEffect(() => {
        if (token) {
            // Set token for both default axios and our api instance
            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
        }
    }, [token]);

    const login = (loginData) => {
        const { token, profile } = loginData;
        setUser(profile);
        setToken(token);
        localStorage.setItem('user', JSON.stringify(profile));
        localStorage.setItem('token', token);
        // Set token for both default axios and our api instance
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    };

    const logout = () => {
        setUser(null);
        setToken(null);
        localStorage.removeItem('user');
        localStorage.removeItem('token');
        // Remove token from both default axios and our api instance
        delete axios.defaults.headers.common['Authorization'];
    };

    return (
        <AuthContext.Provider value={{ user, token, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};
