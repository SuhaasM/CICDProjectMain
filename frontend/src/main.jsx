import React from 'react';
import ReactDOM from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';

// Import Mantine styles & Provider
import '@mantine/core/styles.css';
import { MantineProvider } from '@mantine/core';

// Import Layouts and Pages
import RootLayout from './components/RootLayout.jsx';
import App from './App.jsx';
import Login from './components/Login.jsx';
import Signup from './components/Signup.jsx';
import Dashboard from './components/Dashboard.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import AdminDashboard from './components/AdminDashboard.jsx';
import FacultyDashboard from './components/FacultyDashboard.jsx';
import CourseDetailPage from './components/CourseDetailPage.jsx';
import ProfilePage from './components/ProfilePage.jsx';

import { AuthProvider } from './context/AuthContext.jsx';

const router = createBrowserRouter([
  {
    path: "/",
    element: <RootLayout />, // The RootLayout with the Navbar is the parent for all routes
    children: [
      // Public routes
      {
        index: true, // This makes App.jsx the default homepage for the "/" path
        element: <App />,
      },
      {
        path: "login",
        element: <Login />,
      },
      {
        path: "signup",
        element: <Signup />,
      },
      // Protected routes
      {
        element: <ProtectedRoute />,
        children: [
          {
            path: "dashboard",
            element: <Dashboard />,
          },
          {
            path: "admin",
            element: <AdminDashboard />,
          },
          {
            path: "faculty",
            element: <FacultyDashboard />,
          },
          {
            path: "courses/:courseId", // This is now correctly nested
            element: <CourseDetailPage />,
          },
          {
            path: "/profile",
            element: <ProfilePage />,
          },
        ],
      },
    ],
  },
]);

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <MantineProvider theme={{ colorScheme: 'dark' }} withGlobalStyles withNormalizeCSS>
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>
    </MantineProvider>
  </React.StrictMode>
);