import React from 'react';
import Dashboard from './Dashboard';
import FailedTransactions from './FailedTransactions';
import ReplayHistory from './ReplayHistory';
import './App.css'; // You can add your global styles here

function App() {
    return (
        <div className="App">
            <header className="App-header">
                <h1>Transaction Retry and Replay UI</h1>
            </header>
            <main>
                <Dashboard />
                <FailedTransactions />
                <ReplayHistory />
            </main>
        </div>
    );
}

export default App;