import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function ProtectedRoute() {
    const { token } = useAuth();

    // If the user is not authenticated (no token), redirect them to the login page
    if (!token) {
        return <Navigate to="/login" replace />;
    }

    // If the user is authenticated, render the child route component
    return <Outlet />;
}

export default ProtectedRoute;