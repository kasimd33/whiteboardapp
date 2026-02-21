const API_BASE = '';
const WS_BASE = `${window.location.origin}${API_BASE}`;

let token = localStorage.getItem('jwt');
let userId = localStorage.getItem('userId');
let username = localStorage.getItem('username');
let currentBoardId = localStorage.getItem('boardId');
let stompClient = null;
let canvas, ctx;

// DOM Elements
const authSection = document.getElementById('auth-section');
const boardSection = document.getElementById('board-section');
const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const authError = document.getElementById('auth-error');
const strokePreview = document.getElementById('stroke-preview');

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    if (token) {
        if (currentBoardId) {
            showBoardSection();
            initCanvas();
            loadBoardAndConnect();
        } else {
            showBoardSection();
            initCanvas();
            document.getElementById('board-name').textContent = 'Create or join a board';
            showCreateJoinPrompt();
        }
    } else {
        showAuthSection();
    }
    setupEventListeners();
});

function setupEventListeners() {
    document.querySelectorAll('.tab').forEach(tab => {
        tab.addEventListener('click', () => switchTab(tab.dataset.tab));
    });
    document.getElementById('login-btn').addEventListener('click', login);
    document.getElementById('register-btn').addEventListener('click', register);
    document.getElementById('logout-btn').addEventListener('click', logout);
    document.getElementById('clear-btn').addEventListener('click', clearBoard);
    document.getElementById('new-board-btn')?.addEventListener('click', () => {
        if (stompClient) stompClient.deactivate();
        stompClient = null;
        showCreateJoinPrompt();
    });

    document.getElementById('stroke-width').addEventListener('input', e => {
        strokePreview.style.width = Math.max(8, e.target.value * 2) + 'px';
        strokePreview.style.height = Math.max(8, e.target.value * 2) + 'px';
    });
}

function switchTab(tab) {
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    document.querySelector(`[data-tab="${tab}"]`).classList.add('active');
    loginForm.classList.toggle('hidden', tab !== 'login');
    registerForm.classList.toggle('hidden', tab !== 'register');
    authError.classList.add('hidden');
}

async function login() {
    const usernameIn = document.getElementById('login-username').value.trim();
    const password = document.getElementById('login-password').value;
    if (!usernameIn || !password) {
        showError('Please enter username and password');
        return;
    }
    try {
        const res = await fetch(`${API_BASE}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: usernameIn, password })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Login failed');
        handleAuthSuccess(data);
    } catch (e) {
        showError(e.message);
    }
}

async function register() {
    const usernameIn = document.getElementById('register-username').value.trim();
    const email = document.getElementById('register-email').value.trim();
    const password = document.getElementById('register-password').value;
    if (!usernameIn || !email || !password) {
        showError('Please fill all fields');
        return;
    }
    if (password.length < 6) {
        showError('Password must be at least 6 characters');
        return;
    }
    try {
        const res = await fetch(`${API_BASE}/api/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: usernameIn, email, password, role: 'PARTICIPANT' })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Registration failed');
        handleAuthSuccess(data);
    } catch (e) {
        showError(e.message);
    }
}

function handleAuthSuccess(data) {
    token = data.token;
    userId = data.userId;
    username = data.username;
    localStorage.setItem('jwt', token);
    localStorage.setItem('userId', userId);
    localStorage.setItem('username', username);

    showCreateJoinPrompt();
}

function showCreateJoinPrompt() {
    const boardId = prompt('Enter Board ID to join (or leave empty to create new board):');
    if (boardId && boardId.trim()) {
        joinBoard(boardId.trim());
    } else if (boardId !== null) {
        createBoard();
    }
}

async function createBoard() {
    if (stompClient) {
        stompClient.deactivate();
        stompClient = null;
    }
    clearCanvas();
    try {
        const res = await fetch(`${API_BASE}/api/boards`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ name: `${username}'s Board` })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Failed to create board');
        currentBoardId = data.id;
        localStorage.setItem('boardId', currentBoardId);
        document.getElementById('board-name').textContent = data.name + ' (ID: ' + data.id + ')';
        showBoardSection();
        initCanvas();
        connectWebSocket();
        loadDrawingHistory();
    } catch (e) {
        showError(e.message);
    }
}

async function joinBoard(boardId) {
    if (stompClient) {
        stompClient.deactivate();
        stompClient = null;
    }
    clearCanvas();
    try {
        const res = await fetch(`${API_BASE}/api/boards/${boardId}/join`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Failed to join board');
        currentBoardId = data.id;
        localStorage.setItem('boardId', currentBoardId);
        document.getElementById('board-name').textContent = data.name + ' (ID: ' + data.id + ')';
        showBoardSection();
        initCanvas();
        connectWebSocket();
        loadDrawingHistory();
    } catch (e) {
        showError(e.message);
    }
}

async function loadBoardAndConnect() {
    try {
        const res = await fetch(`${API_BASE}/api/boards/${currentBoardId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!res.ok) throw new Error('Board not found');
        const data = await res.json();
        document.getElementById('board-name').textContent = data.name + ' (ID: ' + data.id + ')';
        connectWebSocket();
        loadDrawingHistory();
    } catch (e) {
        localStorage.removeItem('boardId');
        showError('Could not load board. Please create or join a new one.');
        showAuthSection();
    }
}

function showAuthSection() {
    authSection.classList.remove('hidden');
    boardSection.classList.add('hidden');
}

function showBoardSection() {
    authSection.classList.add('hidden');
    boardSection.classList.remove('hidden');
    document.getElementById('user-badge').textContent = `@${username}`;
}

function showError(msg) {
    authError.textContent = msg;
    authError.classList.remove('hidden');
}

function logout() {
    token = null;
    userId = null;
    username = null;
    currentBoardId = null;
    localStorage.removeItem('jwt');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('boardId');
    if (stompClient) {
        stompClient.disconnect();
        stompClient = null;
    }
    showAuthSection();
}

function initCanvas() {
    canvas = document.getElementById('whiteboard');
    ctx = canvas.getContext('2d');
    resizeCanvas();
    window.addEventListener('resize', resizeCanvas);

    let isDrawing = false;
    let lastX, lastY;
    let currentTool = 'line';
    let shapeStartX, shapeStartY;

    document.getElementById('tool-select').addEventListener('change', e => currentTool = e.target.value);

    function getCoords(e) {
        const rect = canvas.getBoundingClientRect();
        const scaleX = canvas.width / rect.width;
        const scaleY = canvas.height / rect.height;
        return {
            x: (e.clientX - rect.left) * scaleX,
            y: (e.clientY - rect.top) * scaleY
        };
    }

    canvas.addEventListener('mousedown', e => {
        const { x, y } = getCoords(e);
        isDrawing = true;
        lastX = x;
        lastY = y;
        shapeStartX = x;
        shapeStartY = y;
        if (currentTool === 'text') {
            const text = prompt('Enter text:');
            if (text) {
                sendDrawingEvent('text', { x, y, text });
                drawText(x, y, text);
            }
            isDrawing = false;
        }
    });

    canvas.addEventListener('mousemove', e => {
        if (!isDrawing || currentTool === 'text') return;
        const { x, y } = getCoords(e);

        if (currentTool === 'line') {
            sendDrawingEvent('line', {
                points: [{ x: lastX, y: lastY }, { x, y }],
                color: document.getElementById('color-picker').value,
                strokeWidth: parseFloat(document.getElementById('stroke-width').value)
            });
            drawLine(lastX, lastY, x, y);
        } else if (currentTool === 'rectangle' || currentTool === 'circle') {
            // Preview only - final draw on mouseup
        } else if (currentTool === 'erase') {
            sendDrawingEvent('erase', {
                points: [{ x: lastX, y: lastY }, { x, y }],
                strokeWidth: parseFloat(document.getElementById('stroke-width').value) * 2
            });
            eraseLine(lastX, lastY, x, y);
        }
        lastX = x;
        lastY = y;
    });

    canvas.addEventListener('mouseup', e => {
        if (isDrawing && (currentTool === 'rectangle' || currentTool === 'circle')) {
            const { x, y } = getCoords(e);
            if (currentTool === 'rectangle') {
                const rx = Math.min(shapeStartX, x), ry = Math.min(shapeStartY, y);
                const w = Math.abs(x - shapeStartX), h = Math.abs(y - shapeStartY);
                sendDrawingEvent('rectangle', {
                    x: rx, y: ry, width: w, height: h,
                    color: document.getElementById('color-picker').value,
                    strokeWidth: parseFloat(document.getElementById('stroke-width').value)
                });
                drawRectangle(rx, ry, rx + w, ry + h);
            } else if (currentTool === 'circle') {
                const radius = Math.hypot(x - shapeStartX, y - shapeStartY);
                sendDrawingEvent('circle', {
                    cx: shapeStartX, cy: shapeStartY, radius,
                    color: document.getElementById('color-picker').value,
                    strokeWidth: parseFloat(document.getElementById('stroke-width').value)
                });
                drawCircle(shapeStartX, shapeStartY, radius);
            }
        }
        isDrawing = false;
    });
    canvas.addEventListener('mouseleave', () => isDrawing = false);
}

function resizeCanvas() {
    const container = document.querySelector('.canvas-container');
    const w = container.clientWidth;
    const h = container.clientHeight;
    if (canvas.width !== w || canvas.height !== h) {
        const imgData = ctx.getImageData(0, 0, canvas.width, canvas.height);
        canvas.width = w;
        canvas.height = h;
        ctx.putImageData(imgData, 0, 0);
    }
}

function connectWebSocket() {
    if (!token || !currentBoardId) return;

    const socket = new SockJS(`${WS_BASE}/ws`);
    stompClient = new StompJs.Client({
        webSocketFactory: () => socket,
        connectHeaders: { Authorization: `Bearer ${token}` },
        debug: () => {}
    });

    stompClient.onConnect = () => {
        stompClient.subscribe(`/topic/board/${currentBoardId}`, msg => {
            const dto = JSON.parse(msg.body);
            if (dto.userId !== userId) renderDrawingEvent(dto);
        });
    };

    stompClient.onStompError = () => {};
    stompClient.activate();
}

function sendDrawingEvent(type, coordinates) {
    if (!stompClient || !stompClient.connected) return;

    const dto = {
        drawingType: type,
        coordinates,
        color: document.getElementById('color-picker').value,
        strokeWidth: parseFloat(document.getElementById('stroke-width').value)
    };

    stompClient.publish({
        destination: `/app/board/${currentBoardId}/draw`,
        body: JSON.stringify(dto)
    });
}

function renderDrawingEvent(dto) {
    if (dto.drawingType === 'clear') {
        ctx.fillStyle = '#ffffff';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        return;
    }
    const coords = dto.coordinates || {};
    switch (dto.drawingType) {
        case 'line':
            const pts = coords.points;
            if (pts && pts.length >= 2) drawLine(pts[0].x, pts[0].y, pts[1].x, pts[1].y, coords.color, coords.strokeWidth);
            break;
        case 'rectangle':
            drawRectangle(coords.x, coords.y, coords.x + coords.width, coords.y + coords.height, coords.color, coords.strokeWidth);
            break;
        case 'circle':
            drawCircle(coords.cx, coords.cy, coords.radius, coords.color, coords.strokeWidth);
            break;
        case 'text':
            drawText(coords.x, coords.y, coords.text);
            break;
        case 'erase':
            const ep = coords.points;
            if (ep && ep.length >= 2) eraseLine(ep[0].x, ep[0].y, ep[1].x, ep[1].y, null, coords.strokeWidth);
            break;
    }
}

function drawLine(x1, y1, x2, y2, color, strokeWidth) {
    ctx.strokeStyle = color || document.getElementById('color-picker').value;
    ctx.lineWidth = strokeWidth || document.getElementById('stroke-width').value;
    ctx.lineCap = 'round';
    ctx.beginPath();
    ctx.moveTo(x1, y1);
    ctx.lineTo(x2, y2);
    ctx.stroke();
}

function drawRectangle(x1, y1, x2, y2, color, strokeWidth) {
    ctx.strokeStyle = color || document.getElementById('color-picker').value;
    ctx.lineWidth = strokeWidth || document.getElementById('stroke-width').value;
    ctx.strokeRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
}

function drawCircle(cx, cy, radius, color, strokeWidth) {
    ctx.strokeStyle = color || document.getElementById('color-picker').value;
    ctx.lineWidth = strokeWidth || document.getElementById('stroke-width').value;
    ctx.beginPath();
    ctx.arc(cx, cy, radius, 0, Math.PI * 2);
    ctx.stroke();
}

function drawText(x, y, text) {
    ctx.fillStyle = document.getElementById('color-picker').value;
    ctx.font = `${document.getElementById('stroke-width').value * 4}px sans-serif`;
    ctx.fillText(text, x, y);
}

function eraseLine(x1, y1, x2, y2, _, strokeWidth) {
    ctx.strokeStyle = '#ffffff';
    ctx.lineWidth = strokeWidth || 20;
    ctx.lineCap = 'round';
    ctx.beginPath();
    ctx.moveTo(x1, y1);
    ctx.lineTo(x2, y2);
    ctx.stroke();
}

function clearCanvas() {
    if (ctx) {
        ctx.fillStyle = '#ffffff';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
    }
}

async function loadDrawingHistory() {
    try {
        const res = await fetch(`${API_BASE}/api/boards/${currentBoardId}/drawings`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!res.ok) return;
        const events = await res.json();
        events.forEach(renderDrawingEvent);
    } catch (_) {}
}

function clearBoard() {
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    sendDrawingEvent('clear', {});
}
