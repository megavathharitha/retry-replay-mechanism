import React, { useState, useEffect } from 'react';

function Dashboard() {
    const [status, setStatus] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetch('/api/dashboard/status') // Assuming your backend runs on the same host, different port
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                setStatus(data);
                setLoading(false);
            })
            .catch(error => {
                setError(error);
                setLoading(false);
            });
    }, []);

    if (loading) return <p>Loading dashboard data...</p>;
    if (error) return <p>Error loading dashboard data: {error.message}</p>;

    return (
        <div>
            <h2>Dashboard</h2>
            <p>Total Transactions: {status.totalTransactions}</p>
            <p>Failed Transactions: {status.failedTransactions}</p>
            <p>Scheduled Jobs: {status.scheduledJobsCount}</p>
        </div>
    );
}

export default Dashboard;