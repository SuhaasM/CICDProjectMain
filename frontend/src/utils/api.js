import axios from 'axios';

// Use relative URL for API calls to work with any domain
const API_BASE_URL = '';

// Create an axios instance with default config
const api = axios.create({
    baseURL: API_BASE_URL,
});

// Add a request interceptor to automatically add auth token
api.interceptors.request.use(
    (config) => {
        // Get token directly from localStorage to ensure it's always the latest
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Add a response interceptor to handle common errors
api.interceptors.response.use(
    (response) => {
        return response;
    },
    async (error) => {
        const originalRequest = error.config;
        
        // Check if error is due to JWT token expiration (500 with specific message)
        if (error.response && 
            error.response.status === 500 && 
            error.response.data && 
            error.response.data.message && 
            error.response.data.message.includes('JWT expired') && 
            !originalRequest._retry) {
            
            originalRequest._retry = true;
            
            try {
                // Try to refresh the token
                const refreshResponse = await axios.post(`${API_BASE_URL}/api/auth/refresh`);
                if (refreshResponse.data && refreshResponse.data.token) {
                    // Update token in localStorage
                    localStorage.setItem('token', refreshResponse.data.token);
                    
                    // Update axios headers
                    originalRequest.headers.Authorization = `Bearer ${refreshResponse.data.token}`;
                    
                    // Retry the original request
                    return axios(originalRequest);
                }
            } catch (refreshError) {
                // If refresh fails, logout
                localStorage.removeItem('token');
                localStorage.removeItem('user');
                window.location.href = '/login';
                return Promise.reject(refreshError);
            }
        }
        
        if (error.response) {
            // Handle specific error codes
            switch (error.response.status) {
                case 401:
                    // Unauthorized - token expired or invalid
                    localStorage.removeItem('token');
                    localStorage.removeItem('user');
                    // Redirect to login page
                    window.location.href = '/login';
                    break;
                case 403:
                    // Forbidden - user doesn't have permission
                    console.error('Permission denied:', error.response.data);
                    break;
                case 500:
                    console.error('Server error:', error.response.data);
                    break;
                default:
                    console.error('API error:', error.response.data);
            }
        } else if (error.request) {
            // Request was made but no response received
            console.error('No response received:', error.request);
        } else {
            // Something happened in setting up the request
            console.error('Request error:', error.message);
        }
        return Promise.reject(error);
    }
);

export default api;