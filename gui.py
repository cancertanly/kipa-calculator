import tkinter as tk
from tkinter import ttk, messagebox

class ScaleCalculatorApp:
    def __init__(self, root):
        self.root = root
        self.root.title("Калькулятор сигналов КИПиА и Частотника (4-20 мА)")
        self.root.geometry("640x700")
        self.root.minsize(580, 650)

        # Переменные настроек
        self.var_sig_type = tk.StringVar(value="4-20")
        self.var_sig_min = tk.DoubleVar(value=4.0)
        self.var_sig_max = tk.DoubleVar(value=20.0)
        self.var_sig_unit = tk.StringVar(value="мА")

        self.var_phys_preset = tk.StringVar(value="0-10 бар")
        self.var_phys_min = tk.DoubleVar(value=0.0)
        self.var_phys_max = tk.DoubleVar(value=10.0)
        self.var_phys_unit = tk.StringVar(value="бар")

        self.var_freq_min = tk.DoubleVar(value=0.0)
        self.var_freq_max = tk.DoubleVar(value=50.0)

        # Переменные значений
        self.var_val_sig = tk.StringVar(value="12.000")
        self.var_val_pct = tk.StringVar(value="50.00")
        self.var_val_phys = tk.StringVar(value="5.000")
        self.var_val_freq = tk.StringVar(value="25.00")
        self.var_slider = tk.DoubleVar(value=50.0)

        self.updating = False

        self._create_widgets()
        self.update_from_pct(50.0)

    def _create_widgets(self):
        # Настройка стиля ttk
        style = ttk.Style()
        style.theme_use('clam')

        # Кнопки мастеров настройки ЧРП
        f_vfd_btns = ttk.Frame(self.root)
        f_vfd_btns.pack(fill="x", padx=12, pady=(8, 2))
        ttk.Button(f_vfd_btns, text="🔧 CHINT NVF2G", command=self.open_nvf2g_window).pack(side="left", expand=True, fill="x", padx=1)
        ttk.Button(f_vfd_btns, text="🔧 DELTA VFD-B", command=self.open_delta_window).pack(side="left", expand=True, fill="x", padx=1)
        ttk.Button(f_vfd_btns, text="🔧 DANFOSS FC 302", command=self.open_danfoss_window).pack(side="left", expand=True, fill="x", padx=1)
        ttk.Button(f_vfd_btns, text="🔧 СЕРИЯ 900 / X9", command=self.open_series900_window).pack(side="left", expand=True, fill="x", padx=1)

        # Фрейм параметров
        frame_params = ttk.LabelFrame(self.root, text=" ⚙️ Диапазоны датчика и частотника ", padding=10)
        frame_params.pack(fill="x", padx=12, pady=6)

        # Выбор сигнала
        ttk.Label(frame_params, text="Тип сигнала:").grid(row=0, column=0, sticky="w", padx=4, pady=4)
        cb_sig = ttk.Combobox(frame_params, textvariable=self.var_sig_type, values=["4-20", "0-20", "0-10 В", "2-10 В"], state="readonly", width=10)
        cb_sig.grid(row=0, column=1, padx=4, pady=4)
        cb_sig.bind("<<ComboboxSelected>>", self.on_sig_preset_change)

        ttk.Label(frame_params, text="Мин:").grid(row=0, column=2, padx=2)
        ttk.Entry(frame_params, textvariable=self.var_sig_min, width=6).grid(row=0, column=3, padx=2)
        ttk.Label(frame_params, text="Макс:").grid(row=0, column=4, padx=2)
        ttk.Entry(frame_params, textvariable=self.var_sig_max, width=6).grid(row=0, column=5, padx=2)
        self.lbl_sig_unit = ttk.Label(frame_params, textvariable=self.var_sig_unit)
        self.lbl_sig_unit.grid(row=0, column=6, padx=4)

        # Выбор физической величины
        ttk.Label(frame_params, text="Физ. величина:").grid(row=1, column=0, sticky="w", padx=4, pady=4)
        cb_phys = ttk.Combobox(frame_params, textvariable=self.var_phys_preset, 
                               values=["0-10 бар", "0-16 бар", "0-6 бар", "0-25 бар", "0-100 °C", "-50-150 °C", "0-100 м³/ч", "0-5 м"], 
                               state="readonly", width=10)
        cb_phys.grid(row=1, column=1, padx=4, pady=4)
        cb_phys.bind("<<ComboboxSelected>>", self.on_phys_preset_change)

        ttk.Label(frame_params, text="Мин:").grid(row=1, column=2, padx=2)
        ttk.Entry(frame_params, textvariable=self.var_phys_min, width=6).grid(row=1, column=3, padx=2)
        ttk.Label(frame_params, text="Макс:").grid(row=1, column=4, padx=2)
        ttk.Entry(frame_params, textvariable=self.var_phys_max, width=6).grid(row=1, column=5, padx=2)
        ttk.Label(frame_params, textvariable=self.var_phys_unit).grid(row=1, column=6, padx=4)

        # Выбор частоты ЧРП
        ttk.Label(frame_params, text="Частота ЧРП:").grid(row=2, column=0, sticky="w", padx=4, pady=4)
        ttk.Label(frame_params, text="Fмин:").grid(row=2, column=2, padx=2)
        ttk.Entry(frame_params, textvariable=self.var_freq_min, width=6).grid(row=2, column=3, padx=2)
        ttk.Label(frame_params, text="Fмакс:").grid(row=2, column=4, padx=2)
        ttk.Entry(frame_params, textvariable=self.var_freq_max, width=6).grid(row=2, column=5, padx=2)
        ttk.Label(frame_params, text="Гц").grid(row=2, column=6, padx=4)

        # Фрейм интерактивного расчета
        frame_calc = ttk.LabelFrame(self.root, text=" 🎯 Интерактивный расчет в реальном времени ", padding=12)
        frame_calc.pack(fill="x", padx=12, pady=6)

        # Сетка полей ввода
        grid_calc = ttk.Frame(frame_calc)
        grid_calc.pack(fill="x", pady=4)

        # Сигнал
        box1 = ttk.LabelFrame(grid_calc, text=" ВХОДНОЙ СИГНАЛ ", padding=6)
        box1.pack(side="left", expand=True, fill="both", padx=4)
        e_sig = ttk.Entry(box1, textvariable=self.var_val_sig, font=("Segoe UI", 14, "bold"), justify="center", width=8)
        e_sig.pack(fill="x")
        ttk.Label(box1, textvariable=self.var_sig_unit, font=("Segoe UI", 8)).pack()
        e_sig.bind("<KeyRelease>", lambda e: self.on_sig_edit())

        # Процент ЧРП
        box2 = ttk.LabelFrame(grid_calc, text=" ПРОЦЕНТ ЧРП (%) ", padding=6)
        box2.pack(side="left", expand=True, fill="both", padx=4)
        e_pct = ttk.Entry(box2, textvariable=self.var_val_pct, font=("Segoe UI", 14, "bold"), justify="center", width=8)
        e_pct.pack(fill="x")
        ttk.Label(box2, text="% шкалы", font=("Segoe UI", 8)).pack()
        e_pct.bind("<KeyRelease>", lambda e: self.on_pct_edit())

        # Физическая величина
        box3 = ttk.LabelFrame(grid_calc, text=" ИЗМЕРЕНИЕ ", padding=6)
        box3.pack(side="left", expand=True, fill="both", padx=4)
        e_phys = ttk.Entry(box3, textvariable=self.var_val_phys, font=("Segoe UI", 14, "bold"), justify="center", width=8)
        e_phys.pack(fill="x")
        ttk.Label(box3, textvariable=self.var_phys_unit, font=("Segoe UI", 8)).pack()
        e_phys.bind("<KeyRelease>", lambda e: self.on_phys_edit())

        # Частота ЧРП
        box4 = ttk.LabelFrame(grid_calc, text=" ВЫХОД ЧРП (Гц) ", padding=6)
        box4.pack(side="left", expand=True, fill="both", padx=4)
        e_freq = ttk.Entry(box4, textvariable=self.var_val_freq, font=("Segoe UI", 14, "bold"), justify="center", width=8)
        e_freq.pack(fill="x")
        ttk.Label(box4, text="Гц", font=("Segoe UI", 8)).pack()
        e_freq.bind("<KeyRelease>", lambda e: self.on_freq_edit())

        # Ползунок
        self.slider = ttk.Scale(frame_calc, from_=0.0, to=100.0, variable=self.var_slider, command=self.on_slider_move)
        self.slider.pack(fill="x", pady=10)

        # Быстрые кнопки
        frame_btns = ttk.Frame(frame_calc)
        frame_btns.pack(fill="x", pady=2)
        for pct_val in [0, 25, 50, 75, 100]:
            b = ttk.Button(frame_btns, text=f"{pct_val}%", command=lambda p=pct_val: self.update_from_pct(p))
            b.pack(side="left", expand=True, fill="x", padx=2)

        # Кнопки проверки аварии / обрыва
        frame_alarm_btns = ttk.Frame(frame_calc)
        frame_alarm_btns.pack(fill="x", pady=4)
        b_break = ttk.Button(frame_alarm_btns, text="0 мА (Обрыв датчика)", command=lambda: self.update_from_sig(0.0))
        b_break.pack(side="left", expand=True, fill="x", padx=2)
        b_over = ttk.Button(frame_alarm_btns, text="21 мА (Авария / КЗ)", command=lambda: self.update_from_sig(21.0))
        b_over.pack(side="left", expand=True, fill="x", padx=2)

        # Статус NAMUR
        self.lbl_status = tk.Label(frame_calc, text="✅ Норма", bg="#d1fae5", fg="#065f46", font=("Segoe UI", 9, "bold"), pady=4)
        self.lbl_status.pack(fill="x", pady=6)

        # Таблица калибровочных точек
        frame_table = ttk.LabelFrame(self.root, text=" 📊 Контрольные точки шкалы ", padding=8)
        frame_table.pack(fill="both", expand=True, padx=12, pady=6)

        cols = ("pct", "sig", "phys", "freq")
        self.tree = ttk.Treeview(frame_table, columns=cols, show="headings", height=8)
        self.tree.heading("pct", text="Точка (%)")
        self.tree.heading("sig", text="Сигнал")
        self.tree.heading("phys", text="Величина")
        self.tree.heading("freq", text="Частота (Гц)")
        
        self.tree.column("pct", anchor="center", width=80)
        self.tree.column("sig", anchor="center", width=120)
        self.tree.column("phys", anchor="center", width=120)
        self.tree.column("freq", anchor="center", width=100)

        scrollbar = ttk.Scrollbar(frame_table, orient="vertical", command=self.tree.yview)
        self.tree.configure(yscrollcommand=scrollbar.set)
        
        self.tree.pack(side="left", fill="both", expand=True)
        scrollbar.pack(side="right", fill="y")
        self.tree.bind("<Double-1>", self.on_tree_select)

    def on_tree_select(self, event):
        item = self.tree.selection()
        if item:
            val_pct = float(self.tree.item(item, "values")[0].replace("%", ""))
            self.update_from_pct(val_pct)

    def on_sig_preset_change(self, event=None):
        val = self.var_sig_type.get()
        if val == "4-20":
            self.var_sig_min.set(4.0)
            self.var_sig_max.set(20.0)
            self.var_sig_unit.set("мА")
        elif val == "0-20":
            self.var_sig_min.set(0.0)
            self.var_sig_max.set(20.0)
            self.var_sig_unit.set("мА")
        elif val == "0-10 В":
            self.var_sig_min.set(0.0)
            self.var_sig_max.set(10.0)
            self.var_sig_unit.set("В")
        elif val == "2-10 В":
            self.var_sig_min.set(2.0)
            self.var_sig_max.set(10.0)
            self.var_sig_unit.set("В")
        self.update_from_pct(float(self.var_slider.get()))

    def on_phys_preset_change(self, event=None):
        val = self.var_phys_preset.get()
        if val == "0-10 бар":
            self.var_phys_min.set(0.0)
            self.var_phys_max.set(10.0)
            self.var_phys_unit.set("бар")
        elif val == "0-16 бар":
            self.var_phys_min.set(0.0)
            self.var_phys_max.set(16.0)
            self.var_phys_unit.set("бар")
        elif val == "0-6 бар":
            self.var_phys_min.set(0.0)
            self.var_phys_max.set(6.0)
            self.var_phys_unit.set("бар")
        elif val == "0-25 бар":
            self.var_phys_min.set(0.0)
            self.var_phys_max.set(25.0)
            self.var_phys_unit.set("бар")
        elif val == "0-100 °C":
            self.var_phys_min.set(0.0)
            self.var_phys_max.set(100.0)
            self.var_phys_unit.set("°C")
        elif val == "-50-150 °C":
            self.var_phys_min.set(-50.0)
            self.var_phys_max.set(150.0)
            self.var_phys_unit.set("°C")
        elif val == "0-100 м³/ч":
            self.var_phys_min.set(0.0)
            self.var_phys_max.set(100.0)
            self.var_phys_unit.set("м³/ч")
        elif val == "0-5 м":
            self.var_phys_min.set(0.0)
            self.var_phys_max.set(5.0)
            self.var_phys_unit.set("м")
        self.update_from_pct(float(self.var_slider.get()))

    def get_limits(self):
        try:
            return (self.var_sig_min.get(), self.var_sig_max.get(),
                    self.var_phys_min.get(), self.var_phys_max.get(),
                    self.var_freq_min.get(), self.var_freq_max.get())
        except Exception:
            return 4.0, 20.0, 0.0, 10.0, 0.0, 50.0

    def update_from_pct(self, pct):
        if self.updating: return
        self.updating = True
        s_min, s_max, p_min, p_max, f_min, f_max = self.get_limits()
        ratio = pct / 100.0

        sig = s_min + ratio * (s_max - s_min)
        phys = p_min + ratio * (p_max - p_min)
        freq = f_min + ratio * (f_max - f_min)

        self.var_val_sig.set(f"{sig:.3f}")
        self.var_val_pct.set(f"{pct:.2f}")
        self.var_val_phys.set(f"{phys:.3f}")
        self.var_val_freq.set(f"{freq:.2f}")
        self.var_slider.set(max(0.0, min(100.0, pct)))

        self.check_status(sig, pct)
        self.refresh_table()
        self.updating = False

    def update_from_sig(self, sig):
        if self.updating: return
        self.updating = True
        s_min, s_max, p_min, p_max, f_min, f_max = self.get_limits()
        span = s_max - s_min
        ratio = (sig - s_min) / span if span != 0 else 0
        pct = ratio * 100.0

        phys = p_min + ratio * (p_max - p_min)
        freq = f_min + ratio * (f_max - f_min)

        self.var_val_sig.set(f"{sig:.3f}")
        self.var_val_pct.set(f"{pct:.2f}")
        self.var_val_phys.set(f"{phys:.3f}")
        self.var_val_freq.set(f"{freq:.2f}")
        self.var_slider.set(max(0.0, min(100.0, pct)))

        self.check_status(sig, pct)
        self.refresh_table()
        self.updating = False

    def on_pct_edit(self):
        try:
            val = float(self.var_val_pct.get())
            self.update_from_pct(val)
        except ValueError:
            pass

    def on_sig_edit(self):
        try:
            val = float(self.var_val_sig.get())
            self.update_from_sig(val)
        except ValueError:
            pass

    def on_phys_edit(self):
        try:
            val = float(self.var_val_phys.get())
            if self.updating: return
            self.updating = True
            s_min, s_max, p_min, p_max, f_min, f_max = self.get_limits()
            span = p_max - p_min
            ratio = (val - p_min) / span if span != 0 else 0
            pct = ratio * 100.0

            sig = s_min + ratio * (s_max - s_min)
            freq = f_min + ratio * (f_max - f_min)

            self.var_val_sig.set(f"{sig:.3f}")
            self.var_val_pct.set(f"{pct:.2f}")
            self.var_val_freq.set(f"{freq:.2f}")
            self.var_slider.set(max(0.0, min(100.0, pct)))

            self.check_status(sig, pct)
            self.refresh_table()
            self.updating = False
        except ValueError:
            pass

    def on_freq_edit(self):
        try:
            val = float(self.var_val_freq.get())
            if self.updating: return
            self.updating = True
            s_min, s_max, p_min, p_max, f_min, f_max = self.get_limits()
            span = f_max - f_min
            ratio = (val - f_min) / span if span != 0 else 0
            pct = ratio * 100.0

            sig = s_min + ratio * (s_max - s_min)
            phys = p_min + ratio * (p_max - p_min)

            self.var_val_sig.set(f"{sig:.3f}")
            self.var_val_pct.set(f"{pct:.2f}")
            self.var_val_phys.set(f"{phys:.3f}")
            self.var_slider.set(max(0.0, min(100.0, pct)))

            self.check_status(sig, pct)
            self.refresh_table()
            self.updating = False
        except ValueError:
            pass

    def on_slider_move(self, val):
        self.update_from_pct(float(val))

    def check_status(self, sig, pct):
        s_min, s_max, _, _, _, _ = self.get_limits()
        unit = self.var_sig_unit.get()

        if s_min == 4.0 and s_max == 20.0 and unit == "мА":
            if sig < 3.6:
                self.lbl_status.config(text=f"⚠️ ОБРЫВ ЛИНИИ / ОТКАЗ ДАТЧИКА (I = {sig:.2f} мА < 3.6 мА)", bg="#fee2e2", fg="#991b1b")
            elif sig > 21.0:
                self.lbl_status.config(text=f"🚨 НЕИСПРАВНОСТЬ / КЗ / ЗАШКАЛ (I = {sig:.2f} мА > 21.0 мА)", bg="#fee2e2", fg="#991b1b")
            elif sig < 4.0 or sig > 20.0:
                self.lbl_status.config(text=f"⚠️ Вне рабочего диапазона (I = {sig:.2f} мА)", bg="#fef3c7", fg="#92400e")
            else:
                self.lbl_status.config(text="✅ Сигнал в норме (нормальная работа датчика)", bg="#d1fae5", fg="#065f46")
        else:
            if pct < 0.0 or pct > 100.0:
                self.lbl_status.config(text=f"⚠️ Вне диапазона 0-100% ({pct:.1f}%)", bg="#fef3c7", fg="#92400e")
            else:
                self.lbl_status.config(text="✅ Сигнал в норме", bg="#d1fae5", fg="#065f46")

    def refresh_table(self):
        for row in self.tree.get_children():
            self.tree.delete(row)

        s_min, s_max, p_min, p_max, f_min, f_max = self.get_limits()
        unit_sig = self.var_sig_unit.get()
        unit_phys = self.var_phys_unit.get()

        self.tree.heading("sig", text=f"Сигнал ({unit_sig})")
        self.tree.heading("phys", text=f"Величина ({unit_phys})")

        for pct in [0, 10, 20, 25, 30, 40, 50, 60, 70, 75, 80, 90, 100]:
            ratio = pct / 100.0
            sig = s_min + ratio * (s_max - s_min)
            phys = p_min + ratio * (p_max - p_min)
            freq = f_min + ratio * (f_max - f_min)
            self.tree.insert("", "end", values=(f"{pct}%", f"{sig:.2f} {unit_sig}", f"{phys:.2f} {unit_phys}", f"{freq:.2f} Гц"))

    def open_nvf2g_window(self):
        win = tk.Toplevel(self.root)
        win.title("🔧 CHINT NVF2G: Быстрая ПИД-настройка и справочник параметров")
        win.geometry("740x780")
        win.minsize(640, 600)

        nb = ttk.Notebook(win)
        nb.pack(fill="both", expand=True, padx=6, pady=6)

        # ВКЛАДКА 1: БЫСТРЫЙ ПИД
        tab1 = ttk.Frame(nb)
        nb.add(tab1, text="⚡ Быстрая ПИД-настройка")

        f_calc = ttk.LabelFrame(tab1, text=" 🎯 Расчет целевой уставки для параметра F9.02 ", padding=10)
        f_calc.pack(fill="x", padx=10, pady=8)

        v_min = tk.DoubleVar(value=self.var_phys_min.get())
        v_max = tk.DoubleVar(value=self.var_phys_max.get())
        try:
            v_targ = tk.DoubleVar(value=float(self.var_val_phys.get()))
        except ValueError:
            v_targ = tk.DoubleVar(value=4.0)
        v_res = tk.StringVar(value="40.0 %")

        def recalc_f902(*args):
            try:
                mn = v_min.get()
                mx = v_max.get()
                tg = v_targ.get()
                span = mx - mn
                pct = ((tg - mn) / span) * 100.0 if span != 0 else 0.0
                v_res.set(f"{pct:.1f} %")
            except Exception:
                v_res.set("---")

        r_grid = ttk.Frame(f_calc)
        r_grid.pack(fill="x")
        ttk.Label(r_grid, text="Мин (бар):").grid(row=0, column=0, padx=4)
        e1 = ttk.Entry(r_grid, textvariable=v_min, width=7)
        e1.grid(row=0, column=1, padx=4)
        ttk.Label(r_grid, text="Макс (бар):").grid(row=0, column=2, padx=4)
        e2 = ttk.Entry(r_grid, textvariable=v_max, width=7)
        e2.grid(row=0, column=3, padx=4)
        ttk.Label(r_grid, text="Нужное давление (бар):", font=("Segoe UI", 9, "bold")).grid(row=0, column=4, padx=4)
        e3 = ttk.Entry(r_grid, textvariable=v_targ, width=7, font=("Segoe UI", 10, "bold"))
        e3.grid(row=0, column=5, padx=4)

        e1.bind("<KeyRelease>", lambda e: recalc_f902())
        e2.bind("<KeyRelease>", lambda e: recalc_f902())
        e3.bind("<KeyRelease>", lambda e: recalc_f902())

        f_res = ttk.Frame(f_calc)
        f_res.pack(fill="x", pady=(8, 0))
        ttk.Label(f_res, text="Установите в параметр F9.02 значение:").pack(side="left")
        ttk.Label(f_res, textvariable=v_res, font=("Segoe UI", 13, "bold"), foreground="#007acc").pack(side="left", padx=8)

        recalc_f902()

        f_txt = ttk.LabelFrame(tab1, text=" 📋 Пошаговая инструкция настройки ПИД ", padding=8)
        f_txt.pack(fill="both", expand=True, padx=10, pady=4)

        txt1 = scrolledtext.ScrolledText(f_txt, wrap="word", font=("Consolas", 9), background="#f8f9fa")
        txt1.pack(fill="both", expand=True)

        guide_content = """ПОДКЛЮЧЕНИЕ КЛЕММ И СХЕМА:
1. Датчик давления 4-20 мА:
   • Клемма +24V -> Контакт (+) датчика
   • Клемма AI2  -> Контакт (-) датчика
   • Перемычка COM - 0V: ОБЯЗАТЕЛЬНА!
2. Джампер J1: перевести в положение "I" (токовый вход)
3. Пуск двигателя: сухой контакт (тумблер) между DI1 и DCM

ШАГ 1: БАЗОВЫЕ ПАРАМЕТРЫ ПЧ
• F0.01 = 1      -> Пуск/стоп с внешних клемм DI1-DCM
• F0.03 = 5      -> Источник опорной частоты: ПИД-регулятор
• F0.04 = 50.00  -> Максимальная выходная частота (Гц)
• F0.06 = 20.00  -> Нижний предел частоты (насос не перегревается)
• F0.07 = 10.0 с -> Время разгона (плавный пуск)
• F0.08 = 10.0 с -> Время торможения (защита от гидроудара)
• F0.09 = 1      -> Запрет реверса (вращение только вперед!)

ШАГ 2: КАЛИБРОВКА АНАЛОГОВОГО ВХОДА AI2 ПОД 4-20 мА
• F7.01 = 2.00 В -> Нижний порог 4 мА (при J1 в 'I' это 2.00 В)
• F7.03 = 10.00 В-> Верхний порог 20 мА (10.00 В)

ШАГ 3: НАСТРОЙКА ПИД-РЕГУЛЯТОРА (ГРУППА F9)
• F9.00 = 0      -> Источник задания: цифровая уставка из F9.02
• F9.01 = 1      -> Источник обратной связи: токовый вход AI2
• F9.02 = [расчет] -> Уставка давления в % от шкалы датчика
• F9.03 = 1.50   -> Пропорциональный коэффициент Kp (1.0 - 2.0)
• F9.04 = 2.50 с -> Время интегрирования Ti (2.0 - 4.0 с)
• F9.05 = 0.00 с -> Дифференцирование Td (выключено)

ШАГ 4: НАСТРОЙКА СПЯЩЕГО РЕЖИМА (ПРИ ОТСУТСТВИИ РАСХОДА)
• F9.28 = 1      -> Спящий режим: ВКЛЮЧЕН
• F9.30 = 1      -> Условие засыпания: по частоте
• F9.31 = 25.00  -> Частота перехода в сон (Гц)
• F9.32 = 10.0 с -> Задержка перехода в сон (сек)
• F9.33 = 10.0 % -> Порог пробуждения (% падения давления от уставки)
• F9.34 = 1.0 с  -> Задержка пробуждения
"""
        txt1.insert("1.0", guide_content)
        txt1.config(state="disabled")

        f_btns1 = ttk.Frame(tab1)
        f_btns1.pack(fill="x", padx=10, pady=8)
        ttk.Button(f_btns1, text="📋 Скопировать список параметров ПИД", 
                   command=lambda: [self.root.clipboard_clear(), self.root.clipboard_append(guide_content), messagebox.showinfo("Скопировано", "Параметры скопированы!")]).pack(side="left")
        ttk.Button(f_btns1, text="Закрыть", command=win.destroy).pack(side="right")

        # ВКЛАДКА 2: ВСЕ ПАРАМЕТРЫ И ПРИМЕРЫ
        tab2 = ttk.Frame(nb)
        nb.add(tab2, text="📚 Все параметры и примеры")

        f_all = ttk.LabelFrame(tab2, text=" 📖 Полный каталог параметров и практические схемы NVF2G ", padding=8)
        f_all.pack(fill="both", expand=True, padx=10, pady=8)

        txt2 = scrolledtext.ScrolledText(f_all, wrap="word", font=("Consolas", 9), background="#f8f9fa")
        txt2.pack(fill="both", expand=True)

        full_nvf_text = """================================================================================
           ПОЛНЫЙ СПРАВОЧНИК ПАРАМЕТРОВ CHINT NVF2G И ПРИМЕРЫ
================================================================================

[ ГРУППА F0: ОСНОВНЫЕ ПАРАМЕТРЫ РАБОТЫ И УПРАВЛЕНИЯ ]
• F0.01: Режим пуска/останова (Завод: 0)
         0 - Панель управления (клавиши RUN/STOP)
         1 - Клеммы управления (DI1..DI5, перемычка на DCM)
         2 - Связь по шине RS-485 Modbus
• F0.03: Источник задания частоты (Завод: 0)
         0 - Цифровое задание (кнопки вверх/вниз на пульте)
         1 - Аналоговый вход AI1 (0-10 В)
         2 - Аналоговый вход AI2 (4-20 мА / 0-10 В)
         5 - Выход встроенного ПИД-регулятора
• F0.04: Максимальная выходная частота [10.00 .. 400.00 Гц] (Завод: 50.00 Гц)
• F0.05: Верхний предел рабочей частоты [F0.06 .. F0.04] (Завод: 50.00 Гц)
• F0.06: Нижний предел рабочей частоты [0.00 .. F0.05] (Реком: 20.00 Гц для насосов)
• F0.07: Время разгона 1 [0.1 .. 3600.0 с] (Реком: 8.0 - 15.0 с)
• F0.08: Время торможения 1 [0.1 .. 3600.0 с] (Реком: 8.0 - 12.0 с, защита от гидроудара)
• F0.09: Запрет реверса (Завод: 0)
         0 - Реверс разрешен
         1 - Реверс запрещен (строго рекомендуется для насосов и вентиляторов!)
• F0.10: Несущая частота ШИМ [1.0 .. 15.0 кГц] (Завод: 4.0 кГц; выше = тише мотор)
• F0.16: Сброс на заводские настройки: 1 - полный сброс параметров

[ ГРУППА F1: ДАННЫЕ ЭЛЕКТРОДВИГАТЕЛЯ И АВТОНАСТРОЙКА ]
• F1.00: Номинальная мощность двигателя (кВт по шильдику)
• F1.01: Номинальное напряжение (380 В)
• F1.02: Номинальный ток двигателя (Амперы по паспорту)
• F1.03: Номинальная частота (50.00 Гц)
• F1.04: Номинальная скорость вращения (об/мин, напр. 1450, 2890)
• F1.11: Автонастройка двигателя (0 - нет, 1 - статическая без вращения вала)

[ ГРУППА F2: ВЕКТОРНОЕ УПРАВЛЕНИЕ И КРИВАЯ U/f ]
• F2.00: Закон управления (0 - бессенсорный вектор SVC, 1 - скалярный U/f для насосов)
• F2.01: Ручной подъем пускового момента [0.1 .. 10.0%] (Завод: 2.0%)
• F2.03: Выбор кривой U/f (0 - линейная, 1 - квадратичная энергосберегающая)

[ ГРУППА F5: ДИСКРЕТНЫЕ ВХОДЫ (DI1 - DI5) ]
• F5.00: Клемма DI1 (Завод: 1 -> Пуск Вперед FWD при замыкании на DCM)
• F5.01: Клемма DI2 (Завод: 2 -> Реверс REV, либо 9 -> Сброс аварии Reset)
• F5.02: Клемма DI3 (Реком: 9 -> Внешний сброс аварии)
• F5.03: Клемма DI4 (4 -> Толчковый пуск JOG, 16 -> Многоскоростной режим 1)
• F5.04: Клемма DI5 (12 -> Внешняя аварийная блокировка Coast Stop)

[ ГРУППА F6: ВЫХОДНЫЕ РЕЛЕ ]
• F6.00: Реле TA-TB-TC (1 -> Привод в работе RUN; 2 -> Авария привода Fault)

[ ГРУППА F7: АНАЛОГОВЫЕ ВХОДЫ И ВЫХОДЫ ]
• F7.00: Нижний порог входа AI1 (0.00 В)
• F7.01: Нижний порог входа AI2 (2.00 В = 4 мА при джампере J1 в положении 'I')
• F7.03: Верхний порог входа AI2 (10.00 В = 20 мА)
• F7.06: Постоянная времени фильтра AI2 (0.10 .. 0.50 с)
• F7.07: Функция аналогового выхода AO (0 - текущая частота, 1 - ток мотора)

[ ГРУППА F9: ПИД-РЕГУЛЯТОР И СПЯЩИЙ РЕЖИМ ]
• F9.00: Источник задания ПИД (0 - цифровая уставка из F9.02, 1 - вход AI1)
• F9.01: Источник обратной связи ПИД (0 - AI1 0-10В, 1 - AI2 4-20мА)
• F9.02: Цифровая уставка ПИД [% шкалы датчика] (Формула: (P-Pmin)/(Pmax-Pmin)*100)
• F9.03: Пропорциональный коэффициент Kp [0.00 .. 10.00] (Реком: 1.20 - 2.00)
• F9.04: Время интегрирования Ti [0.01 .. 100.0 с] (Реком: 2.00 - 4.00 с)
• F9.05: Время дифференцирования Td (Реком: 0.000 с для насосов)
• F9.28: Активация спящего режима (0 - выкл, 1 - включен)
• F9.30: Условие засыпания (1 - по выходной частоте)
• F9.31: Частота засыпания [F0.06 .. F0.04] (Реком: 22.00 - 25.00 Гц)
• F9.32: Время задержки засыпания [0.0 .. 3600.0 с] (Реком: 10.0 - 15.0 с)
• F9.33: Порог пробуждения [% от уставки] (Реком: 5.0 - 10.0 %)
• F9.34: Задержка пробуждения (1.0 - 2.0 с)

================================================================================
                    ПРАКТИЧЕСКИЕ ПРИМЕРЫ ПРИМЕНЕНИЯ
================================================================================

[ ПРИМЕР 1: НАСОСНАЯ СТАНЦИЯ ХОЛОДНОГО ВОДОСНАБЖЕНИЯ СО СПЯЩИМ РЕЖИМОМ ]
Задача: поддержание давления 4.0 бар при датчике 0-10 бар (4-20 мА) и автоматический
останов при отсутствии водоразбора.
Подключение:
- Датчик давления: +24V -> Датчик (+); Датчик (-) -> клемма AI2.
- Перемычка COM - 0V установлена.
- Джампер J1 переведен в положение 'I' (ток).
- Разрешение работы: перемычка/тумблер между DI1 и DCM.
Параметры:
F0.01=1, F0.03=5, F0.04=50.00, F0.06=20.00, F0.09=1, F7.01=2.00, F9.00=0,
F9.01=1, F9.02=40.0, F9.03=1.5, F9.04=3.0, F9.28=1, F9.30=1, F9.31=23.00,
F9.32=12.0, F9.33=8.0

[ ПРИМЕР 2: ВЫТЯЖНОЙ ВЕНТИЛЯТОР С ПОТЕНЦИОМЕТРОМ 0-10 В ]
Задача: плавное ручное управление оборотами вентилятора вытяжки потенциометром 10 кОм.
Подключение:
- Потенциометр: крайний 1 -> +10V, движок -> AI1, крайний 2 -> GND.
- Пуск: контакт щита автоматики между DI1 и DCM.
Параметры:
F0.01=1, F0.03=1, F0.07=15.0, F0.08=15.0, F0.09=1, F2.00=1

[ ПРИМЕР 3: ДВУХСКОРОСТНОЙ КОНВЕЙЕР (15 Гц / 50 Гц) ]
Задача: работа на пониженной скорости 15 Гц по кнопке DI1, переход на 50 Гц по концевику DI4.
Подключение: DI1-DCM (Пуск), DI4-DCM (Скорость 2).
Параметры:
F0.01=1, F0.03=0, F5.00=1, F5.03=16, FC.00=15.00, FC.01=50.00
"""
        txt2.insert("1.0", full_nvf_text)
        txt2.config(state="disabled")

        f_btns2 = ttk.Frame(tab2)
        f_btns2.pack(fill="x", padx=10, pady=8)
        ttk.Button(f_btns2, text="📋 Скопировать весь справочник параметров", 
                   command=lambda: [self.root.clipboard_clear(), self.root.clipboard_append(full_nvf_text), messagebox.showinfo("Скопировано", "Справочник скопирован!")]).pack(side="left")
        ttk.Button(f_btns2, text="Закрыть", command=win.destroy).pack(side="right")


    def open_delta_window(self):
        win = tk.Toplevel(self.root)
        win.title("🔧 DELTA VFD-B: Быстрая ПИД-настройка и каталог параметров")
        win.geometry("740x780")
        win.minsize(640, 600)

        nb = ttk.Notebook(win)
        nb.pack(fill="both", expand=True, padx=6, pady=6)

        # ВКЛАДКА 1: БЫСТРЫЙ ПИД
        tab1 = ttk.Frame(nb)
        nb.add(tab1, text="⚡ Быстрая ПИД-настройка")

        f_calc = ttk.LabelFrame(tab1, text=" 🎯 Расчет целевой частоты задания для DELTA VFD-B ", padding=10)
        f_calc.pack(fill="x", padx=10, pady=8)

        v_min = tk.DoubleVar(value=self.var_phys_min.get())
        v_max = tk.DoubleVar(value=self.var_phys_max.get())
        try:
            v_targ = tk.DoubleVar(value=float(self.var_val_phys.get()))
        except ValueError:
            v_targ = tk.DoubleVar(value=4.0)
        v_fmax = tk.DoubleVar(value=self.var_freq_max.get())
        v_res = tk.StringVar(value="20.00 Гц")

        def recalc_delta(*args):
            try:
                mn = v_min.get()
                mx = v_max.get()
                tg = v_targ.get()
                fm = v_fmax.get()
                span = mx - mn
                pct = ((tg - mn) / span) if span != 0 else 0.0
                hz = pct * fm
                v_res.set(f"{hz:.2f} Гц")
            except Exception:
                v_res.set("---")

        r_grid = ttk.Frame(f_calc)
        r_grid.pack(fill="x")
        ttk.Label(r_grid, text="Мин (бар):").grid(row=0, column=0, padx=4)
        e1 = ttk.Entry(r_grid, textvariable=v_min, width=6)
        e1.grid(row=0, column=1, padx=4)
        ttk.Label(r_grid, text="Макс (бар):").grid(row=0, column=2, padx=4)
        e2 = ttk.Entry(r_grid, textvariable=v_max, width=6)
        e2.grid(row=0, column=3, padx=4)
        ttk.Label(r_grid, text="Давление (бар):", font=("Segoe UI", 9, "bold")).grid(row=0, column=4, padx=4)
        e3 = ttk.Entry(r_grid, textvariable=v_targ, width=6, font=("Segoe UI", 10, "bold"))
        e3.grid(row=0, column=5, padx=4)
        ttk.Label(r_grid, text="Fmax (Гц):").grid(row=0, column=6, padx=4)
        e4 = ttk.Entry(r_grid, textvariable=v_fmax, width=6)
        e4.grid(row=0, column=7, padx=4)

        e1.bind("<KeyRelease>", lambda e: recalc_delta())
        e2.bind("<KeyRelease>", lambda e: recalc_delta())
        e3.bind("<KeyRelease>", lambda e: recalc_delta())
        e4.bind("<KeyRelease>", lambda e: recalc_delta())

        f_res = ttk.Frame(f_calc)
        f_res.pack(fill="x", pady=(8, 0))
        ttk.Label(f_res, text="Установите стрелками на пульте задание:").pack(side="left")
        ttk.Label(f_res, textvariable=v_res, font=("Segoe UI", 13, "bold"), foreground="#059669").pack(side="left", padx=8)

        recalc_delta()

        f_txt = ttk.LabelFrame(tab1, text=" 📋 Пошаговая инструкция настройки ПИД Delta VFD-B ", padding=8)
        f_txt.pack(fill="both", expand=True, padx=10, pady=4)

        txt1 = scrolledtext.ScrolledText(f_txt, wrap="word", font=("Consolas", 9), background="#f8f9fa")
        txt1.pack(fill="both", expand=True)

        delta_guide = """ПОДКЛЮЧЕНИЕ КЛЕММ И СХЕМА:
1. Датчик давления 4-20 мА:
   • Клемма +24V -> Контакт (+) датчика
   • Клемма ACI  -> Контакт (-) датчика
   • Перемычка ACM - 0V: ОБЯЗАТЕЛЬНА!
2. Пуск двигателя: сухой контакт (тумблер) между клеммами FWD и DCM

ШАГ 1: БАЗОВЫЕ НАСТРОЙКИ (ГРУППЫ 00, 01, 02)
• 00-02 = 09      -> Сброс к заводским установкам 50 Гц
• 01-00 = 50.00 Гц-> Максимальная частота (Fmax)
• 01-07 = 20.00 Гц-> Минимальная рабочая частота насоса
• 01-09 = 10.0 с  -> Время разгона (Ramp up)
• 01-10 = 10.0 с  -> Время замедления (Ramp down)
• 02-00 = 00      -> Источник задания: цифровая панель управления
• 02-01 = 01      -> Источник команды ПУСК: внешние клеммы FWD/DCM
• 02-04 = 01      -> Запрет реверса (вращение только вперед!)

ШАГ 2: НАСТРОЙКА ПИД-РЕГУЛЯТОРА (ГРУППА 10)
• 10-00 = 02      -> ПИД включен, обратная связь с токового входа ACI (4-20 мА)
• 10-01 = 1.00    -> Множитель обратной связи (1.00)
• 10-02 = 2.0     -> Пропорциональный коэффициент P (1.0 - 2.5)
• 10-03 = 2.50 с  -> Время интегрирования I (2.0 - 4.0 сек)
• 10-04 = 0.00 с  -> Время дифференцирования D (выключено)
• 10-08 = 01      -> Контроль обрыва датчика ОС (< 4 мА)
• 10-09 = 01      -> Реакция на обрыв: останов и авария

ШАГ 3: СПЯЩИЙ РЕЖИМ (ГРУППА 11: FAN & PUMP)
• 11-05 = 10.0 с  -> Время задержки перехода в сон и пробуждения
• 11-06 = 25.00 Гц-> Частота засыпания (Sleep Freq) при отсутствии расхода
• 11-07 = 28.00 Гц-> Частота пробуждения (Wakeup Freq, строго больше 11-06)
"""
        txt1.insert("1.0", delta_guide)
        txt1.config(state="disabled")

        f_btns1 = ttk.Frame(tab1)
        f_btns1.pack(fill="x", padx=10, pady=8)
        ttk.Button(f_btns1, text="📋 Скопировать список параметров Delta", 
                   command=lambda: [self.root.clipboard_clear(), self.root.clipboard_append(delta_guide), messagebox.showinfo("Скопировано", "Параметры скопированы!")]).pack(side="left")
        ttk.Button(f_btns1, text="Закрыть", command=win.destroy).pack(side="right")

        # ВКЛАДКА 2: ВСЕ ПАРАМЕТРЫ И ПРИМЕРЫ
        tab2 = ttk.Frame(nb)
        nb.add(tab2, text="📚 Все параметры и примеры")

        f_all = ttk.LabelFrame(tab2, text=" 📖 Полный каталог параметров и примеры DELTA VFD-B ", padding=8)
        f_all.pack(fill="both", expand=True, padx=10, pady=8)

        txt2 = scrolledtext.ScrolledText(f_all, wrap="word", font=("Consolas", 9), background="#f8f9fa")
        txt2.pack(fill="both", expand=True)

        full_delta_text = """================================================================================
           ПОЛНЫЙ КАТАЛОГ ПАРАМЕТРОВ DELTA VFD-B И ПРИМЕРЫ
================================================================================

[ ГРУППА 00: СИСТЕМНЫЕ ПАРАМЕТРЫ И ИНДИКАЦИЯ ]
• 00-02: Сброс параметров к заводским
         09 - Сброс к заводским установкам 50 Гц (европейский стандарт)
         10 - Сброс к 60 Гц
• 00-03: Отображение на основном дисплее
         00 - Выходная частота привода (F, Гц)
         01 - Выходной ток мотора (A)
         02 - Пользовательская величина (K x F, напр. давление, об/мин)
• 00-05: Пользовательский коэффициент масштабирования K

[ ГРУППА 01: БАЗОВЫЕ ПАРАМЕТРЫ ЧАСТОТЫ И ДВИГАТЕЛЯ ]
• 01-00: Максимальная выходная частота Fmax [50.00 .. 400.00 Гц] (Завод: 50.00 Гц)
• 01-01: Номинальная частота двигателя (50.00 Гц)
• 01-02: Максимальное напряжение питания (380.0 В)
• 01-07: Нижний предел выходной частоты [0.10 .. 400.00 Гц] (Реком: 20.00 Гц для насосов)
• 01-09: Время разгона 1 [0.1 .. 3600.0 с] (Реком: 8.0 - 12.0 с)
• 01-10: Время замедления 1 [0.1 .. 3600.0 с] (Реком: 8.0 - 12.0 с, защита от гидроудара)
• 01-12: Выбор S-кривой разгона/торможения (0 - линейная, 1..7 - сглаженная)

[ ГРУППА 02: ИСТОЧНИКИ УПРАВЛЕНИЯ И ТОРМОЖЕНИЕ ]
• 02-00: Источник опорной частоты
         00 - Цифровая панель управления (кнопки вверх/вниз)
         01 - Аналоговый вход AVI (напряжение 0-10 В)
         02 - Аналоговый вход ACI (ток 4-20 мА)
         03 - Последовательный порт RS-485 Modbus
• 02-01: Источник команды ПУСК
         00 - Кнопка RUN на панели
         01 - Внешние клеммы управления (кнопка STOP активна)
         02 - Внешние клеммы (кнопка STOP отключена)
• 02-02: Способ остановки двигателя (00 - по рампе замедления, 01 - самовыбегом)
• 02-04: Запрет реверса (00 - разрешен, 01 - реверс строго заблокирован)

[ ГРУППА 04: МНОГОФУНКЦИОНАЛЬНЫЕ ДИСКРЕТНЫЕ ВХОДЫ (M0 - M5) ]
• 04-00 .. 04-05: Функции клемм M0..M5 (01 - скорость 1, 06 - сброс ошибки Reset)
• 04-07: Режим внешнего клеммного управления
         00 - 2-проводной режим: FWD/STOP, REV/STOP
         01 - 2-проводной режим: RUN/STOP, FWD/REV
         02 - 3-проводной импульсный режим (кнопка Пуск NO, кнопка Стоп NC)

[ ГРУППА 05: ВЫХОДНЫЕ РЕЛЕ ]
• 05-00: Реле RA-RB-RC (01 - индикация Аварии Fault, 02 - Привод в работе Running)

[ ГРУППА 07: ЗАЩИТА И ПАРАМЕТРЫ ДВИГАТЕЛЯ ]
• 07-00: Номинальный ток мотора FLA (Амперы по шильдику)
• 07-03: Электронное термореле (00 - стандартный мотор, 01 - инверторный мотор)

[ ГРУППА 10: ВСТРОЕННЫЙ ПИД-РЕГУЛЯТОР ]
• 10-00: Конфигурация ПИД
         00 - ПИД выключен
         01 - ПИД включен, прямая ОС с входа AVI (0-10 В)
         02 - ПИД включен, прямая ОС с входа ACI (4-20 мА)
         04 - ПИД включен, обратная ОС с входа ACI
• 10-01: Масштабный коэффициент обратной связи (1.00)
• 10-02: Пропорциональный коэффициент P (1.0 .. 2.5)
• 10-03: Интегральное время I (2.00 .. 4.00 с)
• 10-04: Дифференциальное время D (0.00 с для насосов)
• 10-08: Обнаружение обрыва датчика обратной связи (00 - выкл, 01 - включено)
• 10-09: Действие при обрыве датчика (00 - предупреждение, 01 - останов по рампе, 02 - авария Err9)

[ ГРУППА 11: УПРАВЛЕНИЕ НАСОСОМ И ВЕНТИЛЯТОРОМ (СПЯЩИЙ РЕЖИМ) ]
• 11-05: Задержка перехода в спящий режим [0.0 .. 3600.0 с] (Реком: 10.0 - 15.0 с)
• 11-06: Частота спящего режима [0.00 .. 400.00 Гц] (Реком: 22.00 - 25.00 Гц)
• 11-07: Частота пробуждения [0.00 .. 400.00 Гц] (Реком: 26.00 - 30.00 Гц, строго > 11-06)

================================================================================
                    ПРАКТИЧЕСКИЕ ПРИМЕРЫ ПРИМЕНЕНИЯ
================================================================================

[ ПРИМЕР 1: ПОДДЕРЖАНИЕ ДАВЛЕНИЯ ВОДЫ С ДАТЧИКОМ 4-20 мА И РЕЖИМОМ СНА ]
Задача: насос держит давление 3.5 бар (датчик 0-10 бар, 4-20 мА). При отсутствии
водоразбора засыпает на 22 Гц.
Подключение:
- Датчик: +24V -> Датчик (+) -> Датчик (-) -> клемма ACI.
- Перемычка ACM - 0V установлена.
- Клеммы управления: тумблер между FWD и DCM.
Параметры:
00-02=09, 01-00=50.00, 01-07=20.00, 01-09=10.0, 01-10=10.0, 02-00=00, 02-01=01,
02-04=01, 10-00=02, 10-02=2.0, 10-03=3.0, 10-04=0.00, 10-08=01, 10-09=01,
11-05=12.0, 11-06=22.00, 11-07=27.00.
(Задание уставки стрелками пульта: 17.50 Гц = 3.5 бар).

[ ПРИМЕР 2: 3-ПРОВОДНАЯ СХЕМА КНОПОЧНОГО ПОСТА (ПУСК / СТОП БЕЗ ФИКСАЦИИ) ]
Задача: пуск двигателя кнопкой "Пуск" (НО) и останов кнопкой "Стоп" (НЗ).
Подключение:
- Кнопка "Пуск" (НО): между M0 (FWD) и DCM.
- Кнопка "Стоп" (НЗ): между M2 и DCM.
Параметры:
02-01=01, 04-07=02.

[ ПРИМЕР 3: ВЕНТИЛЯТОР С ВНЕШНИМ УПРАВЛЕНИЕМ 0-10 В ]
Задача: регулировка оборотов по сигналу 0-10 В от контроллера здания.
Подключение: сигнал 0-10 В на клеммы AVI (+) и ACM (-). Разрешение на FWD-DCM.
Параметры:
02-00=01, 02-01=01, 01-09=15.0, 01-10=15.0
"""
        txt2.insert("1.0", full_delta_text)
        txt2.config(state="disabled")

        f_btns2 = ttk.Frame(tab2)
        f_btns2.pack(fill="x", padx=10, pady=8)
        ttk.Button(f_btns2, text="📋 Скопировать полный каталог параметров", 
                   command=lambda: [self.root.clipboard_clear(), self.root.clipboard_append(full_delta_text), messagebox.showinfo("Скопировано", "Каталог скопирован!")]).pack(side="left")
        ttk.Button(f_btns2, text="Закрыть", command=win.destroy).pack(side="right")


    def open_danfoss_window(self):
        win = tk.Toplevel(self.root)
        win.title("🔧 DANFOSS VLT FC 302: Быстрая ПИД-настройка и справочник параметров")
        win.geometry("740x780")
        win.minsize(640, 600)

        nb = ttk.Notebook(win)
        nb.pack(fill="both", expand=True, padx=6, pady=6)

        # ВКЛАДКА 1: БЫСТРЫЙ ПИД
        tab1 = ttk.Frame(nb)
        nb.add(tab1, text="⚡ Быстрая ПИД-настройка")

        f_calc = ttk.LabelFrame(tab1, text=" 🎯 Расчет уставки давления для Danfoss VLT FC 302 ", padding=10)
        f_calc.pack(fill="x", padx=10, pady=8)

        v_min = tk.DoubleVar(value=self.var_phys_min.get())
        v_max = tk.DoubleVar(value=self.var_phys_max.get())
        try:
            v_targ = tk.DoubleVar(value=float(self.var_val_phys.get()))
        except ValueError:
            v_targ = tk.DoubleVar(value=4.0)
        v_res = tk.StringVar(value="4.000")

        def recalc_danfoss(*args):
            try:
                tg = v_targ.get()
                v_res.set(f"{tg:.3f}")
            except Exception:
                v_res.set("---")

        r_grid = ttk.Frame(f_calc)
        r_grid.pack(fill="x")
        ttk.Label(r_grid, text="Мин. датчика (бар):").grid(row=0, column=0, padx=4)
        e1 = ttk.Entry(r_grid, textvariable=v_min, width=7)
        e1.grid(row=0, column=1, padx=4)
        ttk.Label(r_grid, text="Макс. датчика (бар):").grid(row=0, column=2, padx=4)
        e2 = ttk.Entry(r_grid, textvariable=v_max, width=7)
        e2.grid(row=0, column=3, padx=4)
        ttk.Label(r_grid, text="Нужное давление (бар):", font=("Segoe UI", 9, "bold")).grid(row=0, column=4, padx=4)
        e3 = ttk.Entry(r_grid, textvariable=v_targ, width=7, font=("Segoe UI", 10, "bold"))
        e3.grid(row=0, column=5, padx=4)

        e1.bind("<KeyRelease>", lambda e: recalc_danfoss())
        e2.bind("<KeyRelease>", lambda e: recalc_danfoss())
        e3.bind("<KeyRelease>", lambda e: recalc_danfoss())

        f_res = ttk.Frame(f_calc)
        f_res.pack(fill="x", pady=(8, 0))
        ttk.Label(f_res, text="Установите в параметр 3-10[0] точное значение:").pack(side="left")
        ttk.Label(f_res, textvariable=v_res, font=("Segoe UI", 13, "bold"), foreground="#dc2626").pack(side="left", padx=8)

        recalc_danfoss()

        f_txt = ttk.LabelFrame(tab1, text=" 📋 Пошаговая инструкция настройки Danfoss VLT FC 302 ", padding=8)
        f_txt.pack(fill="both", expand=True, padx=10, pady=4)

        txt1 = scrolledtext.ScrolledText(f_txt, wrap="word", font=("Consolas", 9), background="#f8f9fa")
        txt1.pack(fill="both", expand=True)

        danfoss_guide = """ПОДКЛЮЧЕНИЕ КЛЕММ И СХЕМА:
1. Датчик давления 4-20 мА:
   • Клемма 12 (+24V Out) -> Контакт (+) датчика
   • Клемма 54 (AI токовый) -> Контакт (-) датчика
   • Переключатель A54 (под LCP): перевести в положение "I" (ток)!
2. Клемма 37 (Safe Stop): ОБЯЗАТЕЛЬНО соединить с клеммой 12 (+24V)! Иначе Alarm 68.
3. Пуск двигателя: сухой контакт (тумблер) между клеммой 12 (+24V) и клеммой 18 (Start)

ШАГ 1: БАЗОВЫЕ НАСТРОЙКИ (ГРУППЫ 1, 3, 4, 5)
• 1-00 = [3] Process Closed Loop -> Замкнутый контур процесса (ПИД)
• 1-03 = [1] Variable Torque     -> Переменный момент (для насосов)
• 4-10 = [0] Clockwise           -> Вращение только по часовой стрелке (запрет реверса!)
• 4-12 = 20.0 Hz                 -> Минимальная скорость двигателя (Гц)
• 4-14 = 50.0 Hz                 -> Максимальная скорость двигателя (Гц)
• 3-41 = 10.00 s                 -> Время разгона (Ramp 1)
• 3-42 = 10.00 s                 -> Время замедления (Ramp 1)
• 5-10 = [8] Start               -> Назначение клеммы 18: Пуск

ШАГ 2: КАЛИБРОВКА ДАТЧИКА (КЛЕММА 54, ГРУППА 6)
• 6-22 = 4.00 mA                 -> Мин. ток клеммы 54 (живой ноль)
• 6-23 = 20.00 mA                -> Макс. ток клеммы 54
• 6-24 = 0.000                   -> Мин. физ. величина (бар)
• 6-25 = [шкала датчика]         -> Макс. физ. величина (например 10.000 бар)

ШАГ 3: НАСТРОЙКА ПИД-РЕГУЛЯТОРА ПРОЦЕССА (ГРУППА 7)
• 7-20 = [2] AI 54               -> Источник обратной связи = клемма 54
• 7-30 = [0] Normal              -> Нормальное регулирование
• 7-31 = [1] On                  -> Защита от насыщения интегратора (Anti Windup)
• 7-33 = 1.00                    -> Пропорциональный коэффициент Kp (0.8 - 1.5)
• 7-34 = 3.00 s                  -> Интегральное время Ti (2.5 - 5.0 сек)
• 7-35 = 0.00 s                  -> Дифференцирование Td (выключено)

ШАГ 4: СПЯЩИЙ РЕЖИМ (ГРУППА 22: APPLICATION FUNCTIONS)
• 22-22 = [1] Enabled            -> Контроль низкой скорости (активен)
• 22-23 = [1] Sleep Mode         -> Действие при отсутствии расхода: спящий режим
• 22-40 = 10 s                   -> Мин. время работы насоса перед уходом в сон
• 22-41 = 10 s                   -> Мин. время нахождения в спящем режиме
• 22-44 = 10.0 %                 -> Порог пробуждения: падение давления на 10% от уставки

ВАЖНО: на панели LCP обязательно нажмите кнопку [Auto On]!
"""
        txt1.insert("1.0", danfoss_guide)
        txt1.config(state="disabled")

        f_btns1 = ttk.Frame(tab1)
        f_btns1.pack(fill="x", padx=10, pady=8)
        ttk.Button(f_btns1, text="📋 Скопировать список параметров Danfoss", 
                   command=lambda: [self.root.clipboard_clear(), self.root.clipboard_append(danfoss_guide), messagebox.showinfo("Скопировано", "Параметры скопированы!")]).pack(side="left")
        ttk.Button(f_btns1, text="Закрыть", command=win.destroy).pack(side="right")

        # ВКЛАДКА 2: ВСЕ ПАРАМЕТРЫ И ПРИМЕРЫ
        tab2 = ttk.Frame(nb)
        nb.add(tab2, text="📚 Все параметры и примеры")

        f_all = ttk.LabelFrame(tab2, text=" 📖 Полный справочник параметров и примеры DANFOSS VLT FC 302 ", padding=8)
        f_all.pack(fill="both", expand=True, padx=10, pady=8)

        txt2 = scrolledtext.ScrolledText(f_all, wrap="word", font=("Consolas", 9), background="#f8f9fa")
        txt2.pack(fill="both", expand=True)

        full_danfoss_text = """================================================================================
       ПОЛНЫЙ СПРАВОЧНИК ПАРАМЕТРОВ DANFOSS VLT® FC 302 И ПРИМЕРЫ
================================================================================

[ ГРУППА 0: УПРАВЛЕНИЕ И ОТОБРАЖЕНИЕ (OPERATION & DISPLAY) ]
• 0-01: Язык интерфейса панели LCP ([0] English, [28] Русский)
• 0-02: Единица индикации частоты ([0] RPM об/мин, [1] Hz Гц)
• 0-03: Региональные установки ([0] Международные 50 Гц)
• 0-20 .. 0-24: Строки индикации на дисплее (частота, ток, уставка, обратная связь)
• 0-51: Сброс к заводским установкам ([1] Инициализация при перезапуске питания)

[ ГРУППА 1: НАГРУЗКА И ДВИГАТЕЛЬ (LOAD & MOTOR) ]
• 1-00: Режим конфигурации
        [0] Разомкнутый контур (Open loop, управление скоростью)
        [3] Замкнутый контур процесса (Process Closed Loop, ПИД-регулятор)
• 1-03: Характеристика крутящего момента
        [0] Постоянный момент (станки, конвейеры)
        [1] Переменный/квадратичный крутящий момент (центробежные насосы, вентиляторы)
        [2] Автоматическая оптимизация энергопотребления (AEO)
• 1-20: Мощность двигателя в кВт (по шильдику)
• 1-22: Номинальное напряжение мотора (380-400 В)
• 1-23: Номинальная частота двигателя (50 Гц)
• 1-24: Номинальный ток мотора в Амперах
• 1-25: Номинальная скорость двигателя в об/мин
• 1-29: Автоматическая адаптация к двигателю AMA ([1] Complete AMA)

[ ГРУППА 3: ЗАДАНИЕ И РАМПЫ (REFERENCE / RAMPS) ]
• 3-02: Минимальное задание процесса (0.000 бар)
• 3-03: Максимальное задание процесса (10.000 бар, шкала датчика)
• 3-10 [0]: Предустановленное задание (целевое давление процесса, напр. 4.000 бар)
• 3-15: Источник задания 1 ([0] Без источника, [1] Аналоговый вход 53, [2] Вход 54)
• 3-41: Время разгона 1 (Ramp 1 Up) [8.0 - 15.0 с]
• 3-42: Время замедления 1 (Ramp 1 Down) [8.0 - 15.0 с]

[ ГРУППА 4: ПРЕДЕЛЫ И ПРЕДУПРЕЖДЕНИЯ (LIMITS / WARNINGS) ]
• 4-10: Направление вращения двигателя ([0] Только по часовой стрелке - запрет реверса)
• 4-12: Нижний предел скорости [20.0 .. 25.0 Гц] (защита насоса)
• 4-14: Верхний предел скорости (50.0 Гц)
• 4-16: Предел крутящего момента двигателя (110 - 150%)

[ ГРУППА 5: ЦИФРОВЫЕ ВХОДЫ И РЕЛЕ (DIGITAL IN/OUT) ]
• 5-10: Клемма 18 (Цифровой вход) -> [8] Пуск (Start)
• 5-11: Клемма 19 (Цифровой вход) -> [1] Сброс аварии (Reset) / [10] Реверс
• 5-12: Клемма 27 -> [0] Не используется (если нет сигнала Safe Stop)
• 5-40: Реле 1 (Клеммы 01-02-03) -> [9] Авария (Alarm)
• 5-41: Реле 2 (Клеммы 04-05-06) -> [5] Привод в работе (Running)

[ ГРУППА 6: АНАЛОГОВЫЕ ВХОДЫ И ВЫХОДЫ (ANALOG IN/OUT) ]
• 6-22: Клемма 54, Мин. ток (4.00 мА)
• 6-23: Клемма 54, Макс. ток (20.00 мА)
• 6-24: Клемма 54, Мин. значение (0.000 бар)
• 6-25: Клемма 54, Макс. значение (10.000 бар)
• 6-26: Время фильтра клеммы 54 (0.10 .. 0.50 с)
• 6-50: Клемма 42 (Аналоговый выход) -> [100] Частота 0-100 Гц (0-20 мА)

[ ГРУППА 7: ПИД-РЕГУЛЯТОР ПРОЦЕССА (PROCESS PID) ]
• 7-20: Источник обратной связи 1 -> [2] Вход 54
• 7-30: Тип регулирования -> [0] Normal (прямое регулирование)
• 7-31: Защита от насыщения интегратора -> [1] On (Anti Windup)
• 7-33: Пропорциональный коэффициент Kp (0.80 .. 1.50)
• 7-34: Время интегрирования Ti (2.50 .. 5.00 с)
• 7-35: Дифференцирование Td (0.00 с)

[ ГРУППА 14: СПЕЦИАЛЬНЫЕ НАСТРОЙКИ ]
• 14-01: Частота коммутации инвертора (4.0 .. 8.0 кГц)
• 14-20: Режим автосброса после сетевых сбоев ([10] 10 попыток автоперезапуска)

[ ГРУППА 22: ПРИКЛАДНЫЕ ФУНКЦИИ И СПЯЩИЙ РЕЖИМ (APPLICATION FUNCTIONS) ]
• 22-22: Контроль низкой скорости -> [1] Включен
• 22-23: Функция нулевого расхода -> [1] Спящий режим (Sleep Mode)
• 22-40: Минимальное время работы до засыпания (10 .. 15 с)
• 22-41: Минимальное время в спящем режиме (10 с)
• 22-44: Разность давлений для пробуждения (5 .. 10 % от уставки)

================================================================================
                    ПРАКТИЧЕСКИЕ ПРИМЕРЫ ПРИМЕНЕНИЯ
================================================================================

[ ПРИМЕР 1: ПОГРУЖНОЙ АРТЕЗИАНСКИЙ НАСОС С АДАПТАЦИЕЙ AMA И РЕЖИМОМ СНА ]
Задача: скважинный насос с кабелем 70 м качает воду в резервуар. Требуется автоадаптация,
защита упорного подшипника (не ниже 25 Гц) и автозасыпание.
Подключение:
- Датчик давления 4-20 мА: Клемма 12 (+24V) -> Датчик (+); Датчик (-) -> Клемма 54.
- Переключатель A54 под дисплеем переведен в положение 'I' (ток).
- Клемма 37 (Safe Stop): соединена перемычкой с клеммой 12 (+24V) для снятия Alarm 68!
- Пуск: перемычка между клеммами 12 (+24V) и 18 (Start).
Параметры:
1-00=[3], 1-03=[1], 1-20..1-25 (паспорт двигателя), 1-29=[1] (запуск AMA в [Hand On]),
4-10=[0], 4-12=25.0 Гц, 6-22=4.00 мА, 6-23=20.00 мА, 6-24=0.0 бар, 6-25=10.0 бар,
7-20=[2], 7-30=[0], 7-33=1.20, 7-34=4.00 с, 3-10[0]=4.000 бар, 14-20=[10], 22-22=[1],
22-23=[1], 22-40=10 с, 22-44=8.0 %.
Обязательно нажать кнопку [Auto On] на пульте LCP!

[ ПРИМЕР 2: ПРИТОЧНО-ВЫТЯЖНАЯ ВЕНТИЛЯЦИЯ С ДАТЧИКОМ ПЕРЕПАДА 0-10 В ]
Задача: поддержание разряжения в воздуховоде по датчику перепада давления 0-500 Па (0-10 В).
Подключение: сигнал 0-10 В на клеммы 53 (+) и 55 (GND). Переключатель A53 в положение 'U'.
Параметры:
1-00=[3], 1-03=[1], 3-03=500.0, 3-10[0]=250.0, 6-10=0.0 В, 6-11=10.0 В,
6-14=0.0, 6-15=500.0, 7-20=[1], 7-33=0.80, 7-34=5.00 с

[ ПРИМЕР 3: ШПИНДЕЛЬ СТАНКА С ПОТЕНЦИОМЕТРОМ И РЕВЕРСОМ ]
Задача: скорость задается внешним потенциометром, реверс тумблером.
Подключение:
- Потенциометр: край 1 -> кл. 50 (+10V), средний -> кл. 53, край 2 -> кл. 55 (GND).
- Тумблер: Вперед: замыкание кл. 12 и 18; Реверс: замыкание кл. 12 и 19.
Параметры:
1-00=[0], 1-03=[0], 3-15=[1], 4-10=[2], 5-10=[8], 5-11=[10]
"""
        txt2.insert("1.0", full_danfoss_text)
        txt2.config(state="disabled")

        f_btns2 = ttk.Frame(tab2)
        f_btns2.pack(fill="x", padx=10, pady=8)
        ttk.Button(f_btns2, text="📋 Скопировать весь справочник параметров", 
                   command=lambda: [self.root.clipboard_clear(), self.root.clipboard_append(full_danfoss_text), messagebox.showinfo("Скопировано", "Справочник скопирован!")]).pack(side="left")
        ttk.Button(f_btns2, text="Закрыть", command=win.destroy).pack(side="right")


    def open_series900_window(self):
        win = tk.Toplevel(self.root)
        win.title("🔧 900 SERIES / X9: Быстрая ПИД-настройка и справочник параметров")
        win.geometry("740x780")
        win.minsize(640, 600)

        nb = ttk.Notebook(win)
        nb.pack(fill="both", expand=True, padx=6, pady=6)

        # ВКЛАДКА 1: БЫСТРЫЙ ПИД
        tab1 = ttk.Frame(nb)
        nb.add(tab1, text="⚡ Быстрая ПИД-настройка")

        f_calc = ttk.LabelFrame(tab1, text=" 🎯 Расчет уставки давления для параметров F5-01 и F5-09 ", padding=10)
        f_calc.pack(fill="x", padx=10, pady=8)

        v_min = tk.DoubleVar(value=self.var_phys_min.get())
        v_max = tk.DoubleVar(value=self.var_phys_max.get())
        try:
            v_targ = tk.DoubleVar(value=float(self.var_val_phys.get()))
        except ValueError:
            v_targ = tk.DoubleVar(value=4.0)
        v_res = tk.StringVar(value="4.0 Bar (40.0 %)")

        def recalc_s900(*args):
            try:
                mn = v_min.get()
                mx = v_max.get()
                tg = v_targ.get()
                span = mx - mn
                pct = ((tg - mn) / span) * 100.0 if span != 0 else 0.0
                v_res.set(f"{tg:.1f} Bar ({pct:.1f} %)")
            except Exception:
                v_res.set("---")

        r_grid = ttk.Frame(f_calc)
        r_grid.pack(fill="x")
        ttk.Label(r_grid, text="Мин. датчика (бар):").grid(row=0, column=0, padx=4)
        e1 = ttk.Entry(r_grid, textvariable=v_min, width=7)
        e1.grid(row=0, column=1, padx=4)
        ttk.Label(r_grid, text="Шкала F5-09 (бар):").grid(row=0, column=2, padx=4)
        e2 = ttk.Entry(r_grid, textvariable=v_max, width=7)
        e2.grid(row=0, column=3, padx=4)
        ttk.Label(r_grid, text="Нужное давление (бар):", font=("Segoe UI", 9, "bold")).grid(row=0, column=4, padx=4)
        e3 = ttk.Entry(r_grid, textvariable=v_targ, width=7, font=("Segoe UI", 10, "bold"))
        e3.grid(row=0, column=5, padx=4)

        e1.bind("<KeyRelease>", lambda e: recalc_s900())
        e2.bind("<KeyRelease>", lambda e: recalc_s900())
        e3.bind("<KeyRelease>", lambda e: recalc_s900())

        f_res = ttk.Frame(f_calc)
        f_res.pack(fill="x", pady=(8, 0))
        ttk.Label(f_res, text="Установите в параметр F5-01 точное значение:").pack(side="left")
        ttk.Label(f_res, textvariable=v_res, font=("Segoe UI", 13, "bold"), foreground="#d97706").pack(side="left", padx=8)

        recalc_s900()

        f_txt = ttk.LabelFrame(tab1, text=" 📋 Пошаговая инструкция настройки ПИД Серии 900 / X9 ", padding=8)
        f_txt.pack(fill="both", expand=True, padx=10, pady=4)

        txt1 = scrolledtext.ScrolledText(f_txt, wrap="word", font=("Consolas", 9), background="#f8f9fa")
        txt1.pack(fill="both", expand=True)

        s900_guide = """ПОДКЛЮЧЕНИЕ КЛЕММ И СХЕМА:
1. Для компактного исполнения 900M (Mini Type):
   • Клемма DC (+24В при параметре F0-06=2) -> Контакт (+) датчика 4-20 мА
   • Клемма AI  -> Контакт (-) датчика давления
   • Клемма GND -> Общий провод
   • Пуск двигателя: сухой контакт (тумблер) между DI1 и GND

2. Для стандартного исполнения 900G (General Type):
   • Клемма 24V -> Контакт (+) датчика давления
   • Клемма AI1 -> Контакт (-) датчика давления
   • Перемычка COM - GND: ОБЯЗАТЕЛЬНА!
   • Пуск двигателя: сухой контакт между DI1 и COM

ШАГ 1: БАЗОВЫЕ НАСТРОЙКИ (ГРУППА F0)
• F0-00 = 1       -> Пуск/стоп с внешних клемм (DI1)
• F0-01 = 6       -> Режим поддержания давления (Constant pressure water supply)
• F0-04 = 10.0 с  -> Время разгона (плавный пуск)
• F0-05 = 10.0 с  -> Время замедления (без гидроудара)
• F0-06 = 2       -> Напряжение выхода клеммы DC: 24В (для 900M)
• F0-07 = 0002    -> Формат входа AI1: 4-20 мА
• F0-08 = 0       -> Останов по рампе замедления
• F0-09 = 50.0 Гц -> Верхний предел рабочей частоты
• F0-10 = 20.0 Гц -> Нижний предел рабочей частоты (защита насоса)
• F0-14 = 0       -> Порядок чередования фаз (0: U-V-W, 1: U-W-V реверс)

ШАГ 2: НАСТРОЙКА ДАТЧИКА И ПИД (ГРУППА F5)
• F5-00 = 0       -> Источник уставки: цифровой параметр F5-01
• F5-01 = [давл]  -> Уставка целевого давления в барах (например 4.0 Bar)
• F5-02 = 0       -> Источник обратной связи: клемма AI1 / AI
• F5-03 = 0       -> Направление регулирования: прямое (давление падает -> разгон)
• F5-04 = 20.0    -> Пропорциональный коэффициент Kp
• F5-05 = 0.80 с  -> Постоянная интегрирования Ki
• F5-08 = 1       -> Тип датчика: 4-20 мА
• F5-09 = [шкала] -> Шкала используемого датчика давления в барах (например 10.0 Bar)

ШАГ 3: СПЯЩИЙ РЕЖИМ И ЗАЩИТА ОТ СУХОГО ХОДА
• F5-12 = 22.0 Гц -> Частота сна (Dormant Frequency) при отсутствии водоразбора
• F5-13 = 10.0 с  -> Задержка перехода в спящий режим
• F5-14 = 8 %     -> Допустимое отклонение давления
• F5-17 = 80 %    -> Порог давления пробуждения (80% от уставки)
• F5-18 = 200 %   -> Защита от избыточного давления (авария Err53)
• F5-19 = 15.0 с  -> Время контроля сухого хода (Water shortage time)
• F5-20 = 48.0 Гц -> Частота контроля сухого хода
• F5-22 = 20 %    -> Порог давления сухого хода (авария A52)
• F5-23 = 20 мин  -> Автоперезапуск после сухого хода
"""
        txt1.insert("1.0", s900_guide)
        txt1.config(state="disabled")

        f_btns1 = ttk.Frame(tab1)
        f_btns1.pack(fill="x", padx=10, pady=8)
        ttk.Button(f_btns1, text="📋 Скопировать список параметров Серии 900", 
                   command=lambda: [self.root.clipboard_clear(), self.root.clipboard_append(s900_guide), messagebox.showinfo("Скопировано", "Параметры скопированы!")]).pack(side="left")
        ttk.Button(f_btns1, text="Закрыть", command=win.destroy).pack(side="right")

        # ВКЛАДКА 2: ВСЕ ПАРАМЕТРЫ И ПРИМЕРЫ
        tab2 = ttk.Frame(nb)
        nb.add(tab2, text="📚 Все параметры и примеры")

        f_all = ttk.LabelFrame(tab2, text=" 📖 Полный справочник параметров и примеры 900 Series / X9 ", padding=8)
        f_all.pack(fill="both", expand=True, padx=10, pady=8)

        txt2 = scrolledtext.ScrolledText(f_all, wrap="word", font=("Consolas", 9), background="#f8f9fa")
        txt2.pack(fill="both", expand=True)

        full_s900_text = """================================================================================
       ПОЛНЫЙ СПРАВОЧНИК ПАРАМЕТРОВ 900 SERIES / X9 (900M / 900G)
================================================================================

[ ГРУППА F0: БАЗОВЫЕ ПАРАМЕТРЫ РАБОТЫ ]
• F0-00: Выбор источника команды (0 - панель, 1 - клеммы DI1..DI5, 2 - RS485 Modbus)
• F0-01: Основной источник задания частоты
         0 - Настройка кодом / энергонезависимая память
         1 - Потенциометр на панели управления
         2 - Аналоговый вход AI1 (0-10В / 4-20мА)
         6 - Режим водоснабжения постоянного давления (Constant pressure water supply)
         7 - Универсальный ПИД-регулятор
• F0-04: Время разгона (0.0 .. 500.0 с, по умолчанию по мощности)
• F0-05: Время замедления (0.0 .. 500.0 с)
• F0-06: Напряжение выхода клеммы DC (для 900M)
         0 - 5 В
         1 - 10 В DC (питание потенциометра)
         2 - 24 В DC (питание датчиков давления 4-20 мА)
• F0-07: Формат аналоговых входов и выходов (0000..5555)
         0: 0-10В, 1: 0-20мА, 2: 4-20мА, 3: 20-4мА, 4: 20-0мА, 5: 10-0В
         Младший разряд (Bit) = AI1 (или AI на 900M); Десятки = AI2; Сотни = AO1
• F0-08: Способ остановки (0 - по рампе замедления, 1 - самовыбегом)
• F0-09: Верхний предел частоты [F0-10 .. 599.9 Гц] (Завод: 50.0 Гц)
• F0-10: Нижний предел частоты [0.0 .. F0-09] (Реком: 20.0 Гц для насосов)
• F0-11: Подъем момента Torque Boost [0.0 .. 30.0%]
• F0-13: Несущая частота ШИМ [0.5 .. 16.0 кГц] (автоматическая адаптация к температуре)
• F0-14: Порядок чередования фаз (0: U-V-W, 1: U-W-V — изменение вращения без перекидки кабеля)
• F0-20: Функция кнопки STOP (0 - только при управлении с клавиатуры, 1 - всегда активна)
• F0-24: Сброс к заводским установкам (1 - восстановить заводские настройки)
• F0-26: Режимы работы группы насосов (0 - ручной, 1 - 1 рабочий/1 резерв, 2..5 - сеть ПЧ)

[ ГРУППА F1: ВХОДЫ И ВЫХОДЫ УПРАВЛЕНИЯ ]
• F1-00 .. F1-04: Функции цифровых входов DI1 .. DI5
         1 - Пуск Вперед FWD
         2 - Пуск Назад REV
         3 - Трехпроводное управление (Стоп)
         7 - Сброс аварии (Fault reset)
         8..10 - Многоскоростные команды 1..3
         12 - Останов самовыбегом (Coast stop)
• F1-06: Режим внешнего управления (0 - 2-проводной 1, 1 - 2-проводной 2, 2 - 3-проводной 1, 3 - 3-проводной 2)
• F1-08: Реле 1 TA-TB-TC (1 - ПЧ работает RUN, 2 - Авария Fault, 3 - Готов Ready)
• F1-24: Усиление аналогового входа AI1 [0.00 .. 20.00] (Завод: 1.00)
• F1-25: Смещение входа AI1 [-10.00 .. +10.00 В] (Калибровка нуля)
• F1-28: Аналоговый выход AO1 (0 - частота, 1 - задание, 2 - ток, 5 - напряжение)

[ ГРУППА F2: ВОЛЬТ-ЧАСТОТНАЯ ХАРАКТЕРИСТИКА V/F ]
• F2-00: Выбор кривой V/F (0 - линейная, 1 - многоточечная, 2 - квадратичная для насосов)
• F2-11: Ток защиты от опрокидывания привода (50..200%)

[ ГРУППА F3: ПУСК, ТОРМОЖЕНИЕ И S-КРИВЫЕ ]
• F3-00: Начальная пусковая частота (0.0 .. 10.0 Гц)
• F3-08: Режим ускорения/замедления (0 - линейный, 1 - S-кривая сглаживания)
• F3-14 / F3-15: Пропуск резонансных частот (Skip frequency и полоса пропускания)
• F3-17: Запрет обратного вращения (0 - разрешен, 1 - реверс строго заблокирован)

[ ГРУППА F5: ПИД, ВОДОСНАБЖЕНИЕ И СПЯЩИЙ РЕЖИМ ]
• F5-00: Источник задания ПИД (0 - параметр F5-01, 1 - AI1, 2 - AI2, 3 - потенциометр)
• F5-01: Уставка давления процесса в барах [0.1 .. 1000.0 Bar] (напр. 4.0 Bar)
• F5-02: Источник обратной связи ПИД (0 - AI1, 1 - AI2, 2 - Modbus)
• F5-03: Направление регулирования (0 - прямое: давление падает -> частота растет)
• F5-04: Пропорциональный коэффициент Kp [0.0 .. 6500.0] (Завод: 20.0)
• F5-05: Время интегрирования Ki [0.01 .. 10.00 с] (Завод: 0.80 с)
• F5-08: Тип датчика (0 - 0~10В, 1 - 4~20мА, 2 - 0~5В, 3 - 0.5~4.5В)
• F5-09: Шкала используемого датчика давления [0.0 .. 25.0 Bar] (напр. 10.0 или 16.0 Bar)
• F5-10: Коррекция нуля датчика (смещение при пустой магистрали)
• F5-12: Частота спящего режима Dormant Frequency [0.0 .. F0-09] (Реком: 22.0 - 25.0 Гц)
• F5-13: Задержка перехода в сон [0.0 .. 1200.0 с] (Реком: 10.0 - 15.0 с)
• F5-14: Допустимое отклонение давления при засыпании (Завод: 8%)
• F5-17: Давление пробуждения Wake up pressure [% от уставки] (Реком: 80%)
• F5-18: Предел максимального давления [% от уставки] (Защита от разрыва труб, авария Err53)
• F5-19: Время контроля сухого хода (15.0 - 30.0 с)
• F5-20: Частота детекции сухого хода (45.0 - 48.0 Гц)
• F5-22: Давление сухого хода (% от уставки, ниже которого выдается авария A52)
• F5-23: Время автоперезапуска после сухого хода (20 - 60 мин)
• F5-25 .. F5-28: Функция защиты от замерзания (Antifreeze)
• F5-32 .. F5-50: Сетевое каскадное управление группой до 5 насосов (Master-Slave)

[ ГРУППА F6: ЗАЩИТЫ, АВТОСБРОС И ДИСПЛЕЙ ]
• F6-03: Пароль пользователя от несанкционированного изменения
• F6-18: Количество попыток автоматического сброса аварий (0..20)
• F6-19: Интервал времени между попытками автосброса (1.0..100.0 с)

[ ГРУППА F7: СВЯЗЬ MODBUS RTU RS-485 ]
• F7-00: Сетевой адрес ПЧ (1..249)
• F7-01: Скорость связи (0 - 9600, 1 - 19200, 2 - 38400, 3 - 57600, 4 - 115200 bps)
• F7-02: Формат кадра (0 - 8-N-2, 1 - 8-E-1, 2 - 8-O-1, 3 - 8-N-1)

[ ГРУППА F8: ДВИГАТЕЛЬ И АВТОНАСТРОЙКА ]
• F8-00: Номинальная мощность двигателя в кВт
• F8-01: Номинальное напряжение мотора (В)
• F8-02: Номинальный ток мотора (А)
• F8-03: Номинальная частота (50.0 Гц)
• F8-04: Номинальные обороты (об/мин, напр. 1460, 2900)
• F8-06: Режим управления (0 - V/F, 1 - векторный бессенсорный IMSVC)
• F8-07: Автонастройка двигателя (0 - нет, 1 - статическая, 2 - динамическая)

[ ГРУППА U0: ОПЕРАТИВНЫЙ МОНИТОРИНГ В РЕАЛЬНОМ ВРЕМЕНИ ]
• U0-00: Состояние ПЧ (1-вперед, 2-реверс, 3-стоп)
• U0-01: Код текущей аварии
• U0-02: Заданная частота (0.1 Гц)
• U0-03: Текущая выходная частота (0.1 Гц)
• U0-06: Выходной ток мотора (0.1 А)
• U0-08: Напряжение шины DC (В)
• U0-13 / U0-14: Напряжение на аналоговых входах AI1 и AI2
• U0-17: Уставка ПИД-регулятора
• U0-18: Текущее значение обратной связи ПИД (фактическое давление)

================================================================================
                    ПРАКТИЧЕСКИЕ ПРИМЕРЫ ПРИМЕНЕНИЯ
================================================================================

[ ПРИМЕР 1: СТАНЦИЯ ВОДОСНАБЖЕНИЯ ХВС С ДАТЧИКОМ 4-20 мА И СПЯЩИМ РЕЖИМОМ ]
Задача: поддержание давления 4.0 бар при датчике 0-10 бар (4-20 мА), автоматический
останов при отсутствии разбора воды и защита от сухого хода (A52).
Подключение (на примере 900M):
- Датчик 4-20 мА: клемма DC (+24В при F0-06=2) -> (+) датчика; (-) датчика -> клемма AI.
- Общий провод: GND.
- Пуск: тумблер между DI1 и GND.
Параметры:
F0-00=1, F0-01=6, F0-04=10.0, F0-05=10.0, F0-06=2, F0-07=0002, F0-10=20.0,
F5-00=0, F5-01=4.0, F5-02=0, F5-04=20.0, F5-05=0.80, F5-08=1, F5-09=10.0,
F5-12=23.0, F5-13=12.0, F5-17=80%, F5-19=15.0, F5-20=48.0, F5-22=20%, F5-23=20.

[ ПРИМЕР 2: ВЕНТИЛЯЦИЯ С ПОТЕНЦИОМЕТРОМ 0-10 В ]
Задача: плавное ручное управление производительностью вытяжного вентилятора.
Подключение:
- Потенциометр 1-5 кОм: край 1 -> +10V, движок -> AI1, край 2 -> GND.
- Разрешение работы: контакт автоматики между DI1 и COM.
Параметры:
F0-00=1, F0-01=2, F0-04=15.0, F0-05=15.0, F0-09=50.0

[ ПРИМЕР 3: 3-ПРОВОДНАЯ СХЕМА КНОПОЧНОГО ПОСТА ПУСК/СТОП ]
Задача: запуск импульсной кнопкой "Пуск" (НО) и останов кнопкой "Стоп" (НЗ).
Подключение:
- Кнопка "Пуск" (НО): между DI1 и GND (или COM).
- Кнопка "Стоп" (НЗ): между DI3 и GND (или COM).
- Кнопка "Реверс": между DI2 и GND.
Параметры:
F0-00=1, F1-06=2 (Three-wire type 1), F1-00=1, F1-01=2, F1-02=3
"""
        txt2.insert("1.0", full_s900_text)
        txt2.config(state="disabled")

        f_btns2 = ttk.Frame(tab2)
        f_btns2.pack(fill="x", padx=10, pady=8)
        ttk.Button(f_btns2, text="📋 Скопировать весь справочник параметров", 
                   command=lambda: [self.root.clipboard_clear(), self.root.clipboard_append(full_s900_text), messagebox.showinfo("Скопировано", "Справочник скопирован!")]).pack(side="left")
        ttk.Button(f_btns2, text="Закрыть", command=win.destroy).pack(side="right")


def main():
    root = tk.Tk()
    app = ScaleCalculatorApp(root)
    root.mainloop()

if __name__ == "__main__":
    main()
