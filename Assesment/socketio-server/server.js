const http = require('http');
const { Server } = require('socket.io');

// Create HTTP server and Socket.IO instance
const server = http.createServer();
const io = new Server(server, {
  cors: {
    origin: "*" // Allow all origins for development
  }
});

// Handle client connections
io.on('connection', (socket) => {
  console.log(`Client connected: ${socket.id}`);

  // Send messages every 5 seconds
  const interval = setInterval(() => {
    const message = `Server time: ${new Date().toISOString()}`;
    socket.emit('message', message); // Send to this client
  }, 5000);

  // Cleanup on disconnect
  socket.on('disconnect', () => {
    console.log(`Client disconnected: ${socket.id}`);
    clearInterval(interval);
  });
});

// Start server
server.listen(3000, () => {
  console.log('Socket.IO server running on http://localhost:3000');
});