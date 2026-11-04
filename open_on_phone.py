import http.server
import socketserver
import socket
import webbrowser
import os

PORT = 8080

def get_local_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        # Не отправляет реальный пакет, просто выбирает активный сетевой маршрут
        s.connect(('8.8.8.8', 80))
        ip = s.getsockname()[0]
    except Exception:
        ip = '127.0.0.1'
    finally:
        s.close()
    return ip

def main():
    ip = get_local_ip()
    url = f"http://{ip}:{PORT}/index.html"
    
    # Создаем красивую страничку с QR-кодом для быстрого сканирования
    qr_page = f"""<!DOCTYPE html>
<html lang="ru">
<head>
  <meta charset="UTF-8">
  <title>Подключение телефона Android</title>
  <style>
    body {{
      background: #0f172a;
      color: #f8fafc;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      margin: 0;
      padding: 20px;
      text-align: center;
    }}
    .card {{
      background: #1e293b;
      border: 1px solid #334155;
      border-radius: 16px;
      padding: 30px;
      max-width: 440px;
      box-shadow: 0 10px 30px rgba(0,0,0,0.5);
    }}
    h1 {{
      font-size: 1.5rem;
      margin-top: 0;
      color: #38bdf8;
    }}
    .qr-container {{
      background: #ffffff;
      padding: 16px;
      border-radius: 12px;
      display: inline-block;
      margin: 20px 0;
    }}
    .url-badge {{
      background: #0f172a;
      color: #38bdf8;
      padding: 10px 14px;
      border-radius: 8px;
      font-family: monospace;
      font-size: 1.1rem;
      word-break: break-all;
      border: 1px solid #334155;
      margin-bottom: 16px;
    }}
    ol {{
      text-align: left;
      font-size: 0.95rem;
      color: #94a3b8;
      line-height: 1.6;
      padding-left: 20px;
    }}
  </style>
</head>
<body>
  <div class="card">
    <h1>📱 Открыть на телефоне Android</h1>
    <p style="color: #94a3b8; font-size: 0.9rem;">Наведите камеру смартфона на QR-код:</p>
    <div class="qr-container">
      <img src="https://api.qrserver.com/v1/create-qr-code/?size=240x240&data={url}" width="240" height="240" alt="QR-код">
    </div>
    <div class="url-badge">{url}</div>
    <div style="margin-bottom: 20px;">
      <a href="./KipaCalculator.apk" download style="display: inline-block; background: #38bdf8; color: #0f172a; text-decoration: none; padding: 12px 20px; border-radius: 8px; font-weight: bold; font-size: 1rem; box-shadow: 0 4px 14px rgba(56, 189, 248, 0.4);">
        📥 Скачать файл KipaCalculator.apk
      </a>
    </div>
    <ol>
      <li>Телефон и компьютер должны быть в одной сети Wi-Fi (или раздайте Wi-Fi с телефона).</li>
      <li>Нажмите кнопку <strong>«Скачать файл KipaCalculator.apk»</strong> и установите APK на Android.</li>
      <li>Или откройте веб-версию и нажмите <strong>«Установить приложение»</strong>.</li>
    </ol>
    <p style="font-size: 0.8rem; color: #64748b; margin-top: 20px;">Приложение работает на телефоне полностью автономно без интернета!</p>
  </div>
</body>
</html>
"""
    with open("qr_connect.html", "w", encoding="utf-8") as f:
        f.write(qr_page)

    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    
    Handler = http.server.SimpleHTTPRequestHandler
    
    print("\n" + "="*60)
    print("⚡ СЕРВЕР ДЛЯ ANDROID ТЕЛЕФОНА ЗАПУЩЕН")
    print("="*60)
    print(f"\n1. Прямой адрес: {url}")
    print("2. Открываем окно с QR-кодом для камеры телефона...")
    print("3. Чтобы остановить сервер, нажмите Ctrl + C в этом окне.\n")
    print("="*60 + "\n")

    webbrowser.open("qr_connect.html")

    with socketserver.TCPServer(("", PORT), Handler) as httpd:
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\nСервер остановлен.")

if __name__ == "__main__":
    main()
