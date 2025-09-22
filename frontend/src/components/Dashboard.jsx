import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../utils/api';
import { Link } from 'react-router-dom';
import { Container, Title, Text, SimpleGrid, Card, Button, Badge } from '@mantine/core';
import { toast } from 'react-toastify';

function Dashboard() {
    const { user, token } = useAuth();
    const [allCourses, setAllCourses] = useState([]);
    const [myCourses, setMyCourses] = useState([]);
    const [loading, setLoading] = useState(true);

    const fetchCourses = async () => {
        if (!token) return;
        setLoading(true);
        
        // Fetch all courses (public endpoint)
        try {
            const allCoursesRes = await api.get('/api/courses');
            setAllCourses(allCoursesRes.data);
        } catch (err) {
            console.error("Failed to fetch all courses", err);
            toast.error("Failed to load available courses.");
        }
        
        // Fetch enrolled courses (protected endpoint)
        try {
            const myCoursesRes = await api.get('/api/enrollments/my-courses');
            setMyCourses(myCoursesRes.data);
        } catch (err) {
            console.error("Failed to fetch enrolled courses", err);
            if (err.response?.status === 403) {
                toast.error("Authentication error. Please try logging in again.");
            } else {
                toast.error("Failed to load your enrolled courses.");
            }
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCourses();
    }, [token]);

    const handleEnroll = async (courseId) => {
        try {
            await api.post('/api/enrollments/request', { courseId });
            toast.success('Enrollment request submitted!');
            // Update the UI to show a "pending" status
            fetchCourses(); // Refresh the course lists
        } catch (err) {
            toast.error(err.response?.data?.error || 'Failed to request enrollment.');
        }
    };
    
    const enrolledCourseIds = new Set(myCourses.map(c => c.id));
    
    if (loading) return <Container><Text>Loading dashboard...</Text></Container>

    return (
        <Container my="lg">
            <Title order={2}>Student Dashboard</Title>
            <Text>Welcome, {user?.username}!</Text>
            
            <section style={{ marginTop: '2rem' }}>
                <Title order={3} mb="md">My Enrolled Courses</Title>
                {myCourses.length > 0 ? (
                    <SimpleGrid cols={3}>
                        {myCourses.map(course => (
                           <Card shadow="sm" padding="lg" radius="md" withborder component={Link} to={`/courses/${course.id}`} key={course.id}>
                               <Text fw={500}>{course.title}</Text>
                           </Card>
                        ))}
                    </SimpleGrid>
                ) : <Text c="dimmed">You are not enrolled in any courses yet.</Text>}
            </section>
            
            <hr style={{ margin: '2rem 0' }} />

            <section>
                <Title order={3} mb="md">All Available Courses</Title>
                <SimpleGrid cols={3}>
                    {allCourses.map(course => (
                       <Card shadow="sm" padding="lg" radius="md" withborder key={course.id}>
                           <Title order={4}>{course.title}</Title>
                           <Text size="sm" c="dimmed" mt="xs">{course.description}</Text>
                           <Button 
                               onClick={() => handleEnroll(course.id)} 
                               disabled={enrolledCourseIds.has(course.id)}
                               fullWidth 
                               mt="md" 
                               radius="md"
                            >
                               {enrolledCourseIds.has(course.id) ? 'Enrolled' : 'Request to Enroll'}
                           </Button>
                       </Card>
                    ))}
                </SimpleGrid>
            </section>
        </Container>
    );
}

export default Dashboard;