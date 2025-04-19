import React, { useState, useEffect } from 'react';

function ReplayHistory() {
    const [replayHistory, setReplayHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetch('/api/replay/history') // Assuming your backend runs on the same host, different port
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                setReplayHistory(data);
                setLoading(false);
            })
            .catch(error => {
                setError(error);
                setLoading(false);
            });
    }, []);

    if (loading) return <p>Loading replay history...</p>;
    if (error) return <p>Error loading replay history: {error.message}</p>;

    return (
        <div>
            <h2>Replay History</h2>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Transaction ID</th>
                        <th>Requested At</th>
                        <th>Replayed By</th>
                        <th>Status</th>
                        <th>Finished At</th>
                        <th>Error Message</th>
                    </tr>
                </thead>
                <tbody>
                    {replayHistory.map(log => (
                        <tr key={log.id}>
                            <td>{log.id}</td>
                            <td>{log.transaction.transactionId}</td>
                            <td>{new Date(log.requestedAt).toLocaleString()}</td>
                            <td>{log.replayedBy}</td>
                            <td>{log.status}</td>
                            <td>{log.finishedAt ? new Date(log.finishedAt).toLocaleString() : 'Pending'}</td>
                            <td>{log.errorMessage}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

export default ReplayHistory;