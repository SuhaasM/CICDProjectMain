import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../utils/api';
import { useAuth } from '../context/useAuth';
import { Container, Title, Paper, TextInput, PasswordInput, Button, Text, Alert } from '@mantine/core';

function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();
    const { login } = useAuth();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        try {
            const response = await api.post('/api/auth/login', { email, password });
            login(response.data);
            
            const userRole = response.data.profile.role;
            switch (userRole) {
                case 'ADMIN':
                    navigate('/admin');
                    break;
                case 'FACULTY':
                    navigate('/faculty');
                    break;
                case 'STUDENT':
                default:
                    navigate('/dashboard');
                    break;
            }
        } catch (err) {
            const msg = (err.response?.data?.error) || (err.response?.data?.message) || err.message || 'Login failed. Please check your credentials.';
            setError(msg);
        }
    };

    return (
        <Container size={420} my={40}>
            <Title ta="center">
                Welcome Back!
            </Title>
            <Text c="dimmed" size="sm" ta="center" mt={5}>
                Do not have an account yet?{' '}
                <Link to="/signup" style={{ color: 'lightblue' }}>Create account</Link>
            </Text>

            <Paper withBorder shadow="md" p={30} mt={30} radius="md">
                <form onSubmit={handleSubmit}>
                    <TextInput label="Email" placeholder="you@example.com" value={email} onChange={(e) => setEmail(e.target.value)} required />
                    <PasswordInput label="Password" placeholder="Your password" value={password} onChange={(e) => setPassword(e.target.value)} required mt="md" />
                    
                    {error && (
                        <Alert color="red" title="Login Error" mt="md" withCloseButton onClose={() => setError('')}>
                            {error}
                        </Alert>
                    )}
                    
                    <Button fullWidth mt="xl" type="submit">
                        Sign in
                    </Button>
                </form>
            </Paper>
        </Container>
    );
}

export default Login;
