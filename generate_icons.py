from PIL import Image, ImageDraw, ImageFont
import os

def create_icon(size, filename):
    # Фон с градиентом или красивым скругленным квадратом
    img = Image.new("RGBA", (size, size), (15, 23, 42, 255))
    draw = ImageDraw.Draw(img)
    
    # Круг / плашка
    margin = int(size * 0.08)
    draw.rounded_rectangle([margin, margin, size - margin, size - margin], radius=int(size * 0.2), fill=(30, 41, 59, 255), outline=(56, 189, 248, 255), width=int(size * 0.03))
    
    # Рисуем стилизованную молнию / датчик 4-20
    # Молния:
    cx, cy = size // 2, size // 2
    scale = size / 100.0
    
    # Координаты молнии
    points = [
        (cx + 5 * scale, cy - 30 * scale),
        (cx - 18 * scale, cy + 2 * scale),
        (cx - 2 * scale, cy + 2 * scale),
        (cx - 8 * scale, cy + 30 * scale),
        (cx + 18 * scale, cy - 4 * scale),
        (cx + 2 * scale, cy - 4 * scale),
    ]
    draw.polygon(points, fill=(56, 189, 248, 255))
    
    # Дополнительная надпись 4-20
    # Простые линии шкалы
    line_y = int(cy + 22 * scale)
    draw.line([cx - 22 * scale, line_y, cx + 22 * scale, line_y], fill=(16, 185, 129, 255), width=int(3 * scale))
    
    img.save(filename, "PNG")
    print(f"Generated {filename}")

if __name__ == "__main__":
    create_icon(192, "icon-192.png")
    create_icon(512, "icon-512.png")
