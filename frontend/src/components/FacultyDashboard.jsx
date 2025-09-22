import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../utils/api';
import { Link } from 'react-router-dom';
import { Container, Title, Paper, TextInput, Textarea, Button, Group, List, ThemeIcon, Text } from '@mantine/core';
import { toast } from 'react-toastify';
import { IconCircleDashed } from '@tabler/icons-react';

function FacultyDashboard() {
    const { token } = useAuth();
    const [courses, setCourses] = useState([]);
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [pending, setPending] = useState([]);

    const fetchData = async () => {
        if (!token) return;
        try {
            const [coursesRes, pendingRes] = await Promise.all([
                api.get('/api/courses'),
                api.get('/api/enrollments/pending')
            ]);
            setCourses(coursesRes.data);
            setPending(pendingRes.data);
        } catch (err) {
            console.error("Failed to fetch data", err);
        }
    };

    useEffect(() => {
        fetchData();
    }, [token]);

    const handleApprove = async (enrollmentId) => {
        try {
            await api.patch(`/api/enrollments/${enrollmentId}/approve`, {});
            toast.success("Enrollment Approved!");
            fetchData();
        } catch (err) {
            toast.error("Failed to approve.");
        }
    };

    const handleCreateCourse = async (e) => {
        e.preventDefault();
        try {
            await api.post('/api/courses', { title, description });
            toast.success("Course created successfully!");
            setTitle('');
            setDescription('');
            fetchData();
        } catch (err) {
            toast.error(err.response?.data?.error || 'Failed to create course.');
        }
    };

    return (
        <Container my="lg">
            <Title order={2} mb="xl">Faculty Dashboard</Title>
            
            <Paper withborder shadow="md" p="md" mb="xl">
                <Title order={3} mb="md">Create New Course</Title>
                <form onSubmit={handleCreateCourse}>
                    <TextInput label="Course Title" placeholder="e.g., Introduction to DevOps" value={title} onChange={(e) => setTitle(e.target.value)} required />
                    <Textarea label="Course Description" placeholder="A brief summary of the course" value={description} onChange={(e) => setDescription(e.target.value)} mt="md" />
                    <Button type="submit" mt="md">Create Course</Button>
                </form>
            </Paper>

            <Title order={3} mb="md">My Courses</Title>
            <List spacing="sm" size="sm" mb="xl">
                {courses.map(course => (
                    <List.Item key={course.id}>
                        <Link to={`/courses/${course.id}`}>{course.title}</Link>
                    </List.Item>
                ))}
            </List>

            <Title order={3} mb="md">Pending Enrollment Requests</Title>
            {pending.length > 0 ? (
                <List spacing="sm" size="sm" center>
                    {pending.map(p => (
                        <List.Item key={p.id} icon={<ThemeIcon color="gray" size={24} radius="xl"><IconCircleDashed size={16} /></ThemeIcon>}>
                           <Group justify="space-between">
                                <Text>{p.student.profile.username} ({p.student.email}) requested to enroll in "{p.course.title}"</Text>
                                <Button size="xs" onClick={() => handleApprove(p.id)}>Approve</Button>
                           </Group>
                        </List.Item>
                    ))}
                </List>
            ) : <Text c="dimmed">No pending requests.</Text>}
        </Container>
    );
}

export default FacultyDashboard;