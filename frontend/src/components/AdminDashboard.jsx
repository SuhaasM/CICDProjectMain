import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../utils/api';
import { Container, Title, Table, Select, Button, Paper, TextInput, PasswordInput, Group } from '@mantine/core';
import { toast } from 'react-toastify';

function AdminDashboard() {
    const { token } = useAuth();
    const [users, setUsers] = useState([]);
    const [enrollments, setEnrollments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [roleChanges, setRoleChanges] = useState({});

    const [newEmail, setNewEmail] = useState('');
    const [newUsername, setNewUsername] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [newRole, setNewRole] = useState('STUDENT');

    const fetchData = async () => {
        if (!token) return;
        setLoading(true);
        try {
            const [usersResponse, enrollmentsResponse] = await Promise.all([
                api.get('/api/admin/users'),
                api.get('/api/admin/enrollments')
            ]);
            setUsers(usersResponse.data);
            setEnrollments(enrollmentsResponse.data);
        } catch (err) {
            setError('Failed to fetch data. You may not have permission.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, [token]);

    const handleCreateUser = async (e) => {
        e.preventDefault();
        try {
            const payload = { email: newEmail, username: newUsername, password: newPassword, role: newRole };
            await api.post('/api/admin/users', payload);
            toast.success('User created successfully!');
            setNewEmail(''); setNewUsername(''); setNewPassword(''); setNewRole('STUDENT');
            fetchData();
        } catch (err) {
            toast.error(err.response?.data || 'Failed to create user.');
        }
    };

    const handleRoleUpdate = async (profileId) => {
        const newRole = roleChanges[profileId] || users.find(u => u.id === profileId)?.role;
        if (!newRole) return;
        try {
            await api.patch(`/api/admin/users/${profileId}/role`, { role: newRole });
            toast.success(`User role updated to ${newRole}`);
            fetchData();
        } catch (err) {
            toast.error('Failed to update role.');
        }
    };

    const handleDeleteEnrollment = async (enrollmentId) => {
        if (window.confirm('Are you sure you want to un-enroll this student?')) {
            try {
                await api.delete(`/api/admin/enrollments/${enrollmentId}`);
                toast.success('Enrollment removed successfully!');
                fetchData();
            } catch (err) {
                toast.error('Failed to remove enrollment.');
            }
        }
    };

    if (loading) return <Container><p>Loading Admin Dashboard...</p></Container>;
    if (error) return <Container><p style={{ color: 'red' }}>{error}</p></Container>;

    return (
        <Container my="lg">
            <Title order={2} mb="xl">Admin Dashboard</Title>
            
            <Paper withborder shadow="md" p="md" mb="xl">
                <Title order={3} mb="md">Create New User</Title>
                <form onSubmit={handleCreateUser}>
                    <Group grow>
                        <TextInput value={newEmail} onChange={(e) => setNewEmail(e.target.value)} placeholder="Email" required />
                        <TextInput value={newUsername} onChange={(e) => setNewUsername(e.target.value)} placeholder="Username" required />
                        <PasswordInput value={newPassword} onChange={(e) => setNewPassword(e.target.value)} placeholder="Password" required />
                    </Group>
                    <Group mt="md">
                        <Select label="Role" value={newRole} onChange={setNewRole} data={['STUDENT', 'FACULTY', 'ADMIN']} />
                        <Button type="submit" mt="md">Create User</Button>
                    </Group>
                </form>
            </Paper>

            <Title order={3} mb="md">User Management</Title>
            <Table striped highlightOnHover withborder>
                <thead><tr><th>Profile ID</th><th>Username</th><th>Current Role</th><th>Change Role To</th><th>Action</th></tr></thead>
                <tbody>{users.map((profile) => (
                    <tr key={profile.id}>
                        <td>{profile.id}</td><td>{profile.username}</td><td>{profile.role}</td>
                        <td>
                            <Select data={['STUDENT', 'FACULTY', 'ADMIN']} defaultValue={profile.role} onChange={(value) => setRoleChanges(prev => ({ ...prev, [profile.id]: value }))} />
                        </td>
                        <td><Button size="xs" onClick={() => handleRoleUpdate(profile.id)}>Update</Button></td>
                    </tr>
                ))}</tbody>
            </Table>

            <Title order={3} mt="xl" mb="md">Enrollment Management</Title>
            <Table striped highlightOnHover withborder>
                <thead><tr><th>Enrollment ID</th><th>Student Username</th><th>Course Title</th><th>Status</th><th>Action</th></tr></thead>
                <tbody>{enrollments.map((enrollment) => (
                    <tr key={enrollment.enrollmentId}>
                        <td>{enrollment.enrollmentId}</td><td>{enrollment.studentUsername}</td><td>{enrollment.courseTitle}</td><td>{enrollment.status}</td>
                        <td><Button size="xs" color="red" onClick={() => handleDeleteEnrollment(enrollment.enrollmentId)}>Un-enroll</Button></td>
                    </tr>
                ))}</tbody>
            </Table>
        </Container>
    );
}

export default AdminDashboard;