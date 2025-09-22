import { Link } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { Container, Title, Text, Button, Group } from '@mantine/core'; // <-- Import Mantine components

function App() {
  const { user } = useAuth();

  return (
    <Container size="sm" style={{ textAlign: 'center', marginTop: '5rem' }}>
      <Title order={1} style={{ marginBottom: '1rem' }}>
        Welcome to the LMS Platform
      </Title>
      
      {user ? (
        <div>
          <Text size="xl">You are logged in as {user.username}.</Text>
          <Text c="dimmed">Explore your dashboard using the navigation bar above.</Text>
        </div>
      ) : (
        <div>
          <Text size="lg" style={{ marginBottom: '1.5rem' }}>
            Please log in or create an account to continue.
          </Text>
          <Group justify="center">
            <Button component={Link} to="/login" variant="gradient" gradient={{ from: 'blue', to: 'cyan' }}>
              Login
            </Button>
            <Button component={Link} to="/signup" variant="default">
              Sign Up
            </Button>
          </Group>
        </div>
      )}
    </Container>
  );
}

export default App;