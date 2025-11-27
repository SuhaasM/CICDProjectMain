import { useState, useEffect } from 'react';
import { useAuth } from '../context/useAuth';
import api from '../utils/api';
import { Container, Title, Paper, TextInput, Button, Text as MantineText, Avatar, FileInput, Group } from '@mantine/core';
import { toast } from 'react-toastify';

function ProfilePage() {
    const { token } = useAuth();
    const [profile, setProfile] = useState(null);
    const [username, setUsername] = useState('');
    const [loading, setLoading] = useState(true);
    const [avatarFile, setAvatarFile] = useState(null); // State for the new file

    const fetchProfile = async () => {
        if (!token) return;
        try {
            const response = await api.get('/api/profile/me');
            setProfile(response.data);
            setUsername(response.data.username);
            setLoading(false);
        } catch (error) {
            toast.error("Failed to load profile");
            console.error("Error fetching profile:", error);
            setLoading(false);
        }
    };
    
    useEffect(() => { 
        fetchProfile(); 
    }, [token]);

    const handleUpdateProfile = async (e) => {
        e.preventDefault();
        try {
            await api.patch('/api/profile/me', { username });
            toast.success("Profile updated successfully!");
            fetchProfile(); // Refresh profile data
        } catch (error) {
            toast.error("Failed to update profile");
            console.error("Error updating profile:", error);
        }
    };

    const handleAvatarUpload = async () => {
        if (!avatarFile) {
            toast.error("Please select a file first.");
            return;
        }
        const formData = new FormData();
        formData.append("file", avatarFile);

        try {
            await api.post('/api/profile/me/avatar', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });
            toast.success("Avatar updated!");
            fetchProfile(); // Refresh profile to show new avatar
        } catch (error) {
            toast.error(error?.response?.data?.error || "Avatar upload failed.");
        }
    };

    if (loading) return <Container my="lg"><MantineText>Loading profile...</MantineText></Container>;

    return (
        <Container size="sm" my="lg">
            <Title order={2} mb="xl">My Profile</Title>
            <Paper withBorder shadow="md" p={30} radius="md" mb="xl">
                <Group justify="center" direction="column">
                    <Avatar src={profile?.avatarUrl ? `http://localhost:12345${profile?.avatarUrl}` : null} size="xl" radius="xl" />
                    <FileInput value={avatarFile} onChange={setAvatarFile} placeholder="Choose an image" style={{ width: '250px' }} accept="image/*" />
                    <Button onClick={handleAvatarUpload} disabled={!avatarFile}>Upload New Avatar</Button>
                </Group>
            </Paper>

            <Paper withBorder shadow="md" p={30} radius="md">
                <form onSubmit={handleUpdateProfile}>
                    <TextInput label="Email" value={profile?.email || ''} disabled />
                    <TextInput label="Username" placeholder="Your username" value={username} onChange={(e) => setUsername(e.target.value)} required mt="md" />
                    <Button type="submit" mt="xl">Update Profile</Button>
                </form>
            </Paper>
        </Container>
    );
}

export default ProfilePage;
