import React, { useState, useEffect } from 'react';

function FailedTransactions() {
    const [failedTransactions, setFailedTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetch('/api/replay/failed') // Assuming your backend runs on the same host, different port
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                setFailedTransactions(data);
                setLoading(false);
            })
            .catch(error => {
                setError(error);
                setLoading(false);
            });
    }, []);

    const handleReplay = (transactionId) => {
        fetch(`/api/replay/trigger/${transactionId}`, {
            method: 'POST',
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.text();
        })
        .then(message => {
            alert(message);
            // Optionally refresh the failed transactions list
            fetch('/api/replay/failed')
                .then(response => response.json())
                .then(data => setFailedTransactions(data));
        })
        .catch(error => {
            console.error("Error triggering replay:", error);
            alert("Failed to trigger replay.");
        });
    };

    if (loading) return <p>Loading failed transactions...</p>;
    if (error) return <p>Error loading failed transactions: {error.message}</p>;

    return (
        <div>
            <h2>Failed Transactions</h2>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Type</th>
                        <th>Status</th>
                        <th>Created At</th>
                        <th>Error</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    {failedTransactions.map(transaction => (
                        <tr key={transaction.id}>
                            <td>{transaction.id}</td>
                            <td>{transaction.transactionType}</td>
                            <td>{transaction.status}</td>
                            <td>{new Date(transaction.createdAt).toLocaleString()}</td>
                            <td>{transaction.errorMessage}</td>
                            <td>
                                <button onClick={() => handleReplay(transaction.id)}>Replay</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

export default FailedTransactions;