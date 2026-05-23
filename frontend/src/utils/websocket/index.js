export function createWebSocket(uri){
  const url = `${import.meta.env.VITE_WEBSOCKET_BASE_URL}${uri}`;
  return new WebSocket(url);
}
