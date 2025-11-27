import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/useAuth';
import { Group, Button, Title } from '@mantine/core';

function Navbar() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <Group h="100%" px="md" justify="space-between">
            <Title order={3} component={Link} to="/" style={{ textDecoration: 'none', color: 'inherit' }}>
                LMS Platform
            </Title>

            <Group>
                {user ? (
                    <>
                        {user.role === 'ADMIN' && <Button component={Link} to="/admin" variant="subtle">Admin Dashboard</Button>}
                        {user.role === 'FACULTY' && <Button component={Link} to="/faculty" variant="subtle">Faculty Dashboard</Button>}
                        {user.role === 'STUDENT' && <Button component={Link} to="/dashboard" variant="subtle">Student Dashboard</Button>}
                        
                        <Button component={Link} to="/profile" variant="subtle">My Profile</Button> {/* <-- ADD THIS LINE */}
                        
                        <Button onClick={handleLogout} variant="light">Logout ({user?.username || 'User'})</Button>
                    </>
                ) : (
                    <>
                        <Button component={Link} to="/login" variant="default">Login</Button>
                        <Button component={Link} to="/signup">Sign Up</Button>
                    </>
                )}
            </Group>
        </Group>
    );
}

export default Navbar;
