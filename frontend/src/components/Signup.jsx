import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../utils/api';
import { Container, Title, Paper, TextInput, PasswordInput, Button, Text, Alert } from '@mantine/core';

function Signup() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [username, setUsername] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        try {
            const payload = { email, password, username };
            await api.post('/api/auth/signup', payload);
            setSuccess('Registration successful! Redirecting to login...');
            setTimeout(() => navigate('/login'), 2000);
        } catch (err) {
            setError(err.response?.data || 'Registration failed. Please try again.');
        }
    };

    return (
        <Container size={420} my={40}>
            <Title ta="center">
                Create Your Account
            </Title>
            <Text c="dimmed" size="sm" ta="center" mt={5}>
                Already have an account?{' '}
                <Link to="/login" style={{ color: 'lightblue' }}>Sign in</Link>
            </Text>

            <Paper withBorder shadow="md" p={30} mt={30} radius="md">
                <form onSubmit={handleSubmit}>
                    <TextInput label="Username" placeholder="Your username" value={username} onChange={(e) => setUsername(e.target.value)} required />
                    <TextInput label="Email" placeholder="you@example.com" value={email} onChange={(e) => setEmail(e.target.value)} required mt="md" />
                    <PasswordInput label="Password" placeholder="Your password" value={password} onChange={(e) => setPassword(e.target.value)} required mt="md" />
                    
                    {error && <Alert color="red" title="Registration Error" mt="md" withCloseButton onClose={() => setError('')}>{error}</Alert>}
                    {success && <Alert color="green" title="Success" mt="md">{success}</Alert>}
                    
                    <Button fullWidth mt="xl" type="submit">
                        Sign up
                    </Button>
                </form>
            </Paper>
        </Container>
    );
}

export default Signup;