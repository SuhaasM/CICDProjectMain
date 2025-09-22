import React from 'react';
import { Outlet } from 'react-router-dom';
import { AppShell } from '@mantine/core';
import Navbar from './Navbar';

function RootLayout() {
    return (
        <AppShell
            header={{ height: 60 }}
            padding="md"
        >
            <AppShell.Header>
                {/* The Navbar component now lives inside the AppShell's header */}
                <Navbar />
            </AppShell.Header>

            <AppShell.Main>
                {/* The Outlet still renders the current page */}
                <Outlet />
            </AppShell.Main>
        </AppShell>
    );
}

export default RootLayout;