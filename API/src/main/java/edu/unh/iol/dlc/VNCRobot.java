package edu.unh.iol.dlc;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;

import static edu.unh.iol.dlc.XKeySym.*;
import static java.awt.event.KeyEvent.*;

class VNCRobot implements IRobot
{
    public static final int VNC_POINTER_EVENT_BUTTON_1 = 1 << 0;
    public static final int VNC_POINTER_EVENT_BUTTON_2 = 1 << 1;
    public static final int VNC_POINTER_EVENT_BUTTON_3 = 1 << 2;
    public static final int VNC_POINTER_EVENT_BUTTON_4 = 1 << 3;
    public static final int VNC_POINTER_EVENT_BUTTON_5 = 1 << 4;
    public static final int VNC_POINTER_EVENT_BUTTON_6 = 1 << 5;
    public static final int VNC_POINTER_EVENT_BUTTON_7 = 1 << 6;
    public static final int VNC_POINTER_EVENT_BUTTON_8 = 1 << 7;

    private final VNCScreen screen;
    private int mouseX;
    private int mouseY;
    private int mouseButtons;
    private int autoDelay;
    private boolean shift;

    public VNCRobot(VNCScreen screen)
    {
        this.screen = screen;
        this.autoDelay = 100;
    }

    @Override
    public ScreenImage captureScreen(Rectangle screenRect)
    {
        return screen.capture(screenRect);
    }

    @Override
    public boolean isRemote()
    {
        return true;
    }

    @Override
    public IScreen getScreen()
    {
        return screen;
    }

    @Override
    public void keyDown(String keys)
    {
        for (int i = 0; i < keys.length(); i++) {
            typeChar(keys.charAt(i), KeyMode.PRESS_ONLY);
        }
    }

    @Override
    public void keyUp(String keys)
    {
        for (int i = 0; i < keys.length(); i++) {
            typeChar(keys.charAt(i), KeyMode.RELEASE_ONLY);
        }
    }

    @Override
    public void keyDown(int code)
    {
        typeKey(code, KeyMode.PRESS_ONLY);
    }

    @Override
    public void keyUp(int code)
    {
        typeCode(keyToXlib(code), KeyMode.RELEASE_ONLY);
    }

    @Override
    public void keyUp()
    {
        // Not implemented
    }

    @Override
    public void pressModifiers(int modifiers)
    {
        typeModifiers(modifiers, KeyMode.PRESS_ONLY);
    }

    @Override
    public void releaseModifiers(int modifiers)
    {
        typeModifiers(modifiers, KeyMode.PRESS_ONLY);
    }

    private void typeModifiers(int modifiers, KeyMode keyMode)
    {
        if ((modifiers & KeyModifier.CTRL) != 0) typeKey(KeyEvent.VK_CONTROL, keyMode);
        if ((modifiers & KeyModifier.SHIFT) != 0) typeCode(KeyEvent.VK_SHIFT, keyMode);
        if ((modifiers & KeyModifier.ALT) != 0) typeCode(KeyEvent.VK_ALT, keyMode);
        if ((modifiers & KeyModifier.ALTGR) != 0) typeCode(KeyEvent.VK_ALT_GRAPH, keyMode);
        if ((modifiers & KeyModifier.META) != 0) typeCode(KeyEvent.VK_META, keyMode);
    }

    @Override
    public void typeStarts()
    {

    }

    @Override
    public void typeEnds()
    {

    }

    @Override
    public void typeKey(int key)
    {
        typeKey(key, KeyMode.PRESS_RELEASE);
    }

    @Override
    public void typeChar(char character, KeyMode mode)
    {
        typeCode(charToXlib(character), mode);
    }

    public void typeKey(int key, KeyMode mode)
    {
        typeCode(keyToXlib(key), mode);
    }

    private void typeCode(int xlibCode, KeyMode mode)
    {
        boolean addShift = requiresShift(xlibCode);
        try {
            if (mode == KeyMode.PRESS_RELEASE || mode == KeyMode.PRESS_ONLY) {
                if (addShift && !shift) {
                    screen.getClient().keyDown(XK_Shift_L);
                }
                screen.getClient().keyDown(xlibCode);
                if (xlibCode == XK_Shift_L || xlibCode == XK_Shift_R || xlibCode == XK_Shift_Lock) {
                    shift = true;
                }
            }

            if (mode == KeyMode.PRESS_RELEASE || mode == KeyMode.RELEASE_ONLY) {
                screen.getClient().keyUp(xlibCode);
                if (addShift && !shift) {
                    screen.getClient().keyUp(XK_Shift_L);
                }

                if (xlibCode == XK_Shift_L || xlibCode == XK_Shift_R || xlibCode == XK_Shift_Lock) {
                    shift = false;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int charToXlib(char c)
    {
        if (c >= 0x0020 && c <= 0x00FF) {
            return c;
        }

        switch (c) {
            case '\u0008':
                return XK_BackSpace;
            case '\u0009':
                return XK_Tab;
            case '\n':
                return XK_Linefeed;
            case '\u000b':
                return XK_Clear;
            case '\r':
                return XK_Return;
            case '\u0013':
                return XK_Pause;
            case '\u0014':
                return XK_Scroll_Lock;
            case '\u0015':
                return XK_Sys_Req;
            case '\u001b':
                return XK_Escape;
            case '\u007f':
                return XK_Delete;
            default:
                throw new IllegalArgumentException("Cannot type character " + c);
        }
    }

    private int keyToXlib(int code)
    {
        switch (code) {
            case VK_ENTER:
                return XK_Return;
            case VK_BACK_SPACE:
                return XK_BackSpace;
            case VK_TAB:
                return XK_Tab;
            case VK_CANCEL:
                return XK_Cancel;
            case VK_CLEAR:
                return XK_Clear;
            case VK_SHIFT:
                return XK_Shift_L;
            case VK_CONTROL:
                return XK_Control_L;
            case VK_ALT:
                return XK_Alt_L;
            case VK_PAUSE:
                return XK_Pause;
            case VK_CAPS_LOCK:
                return XK_Caps_Lock;
            case VK_ESCAPE:
                return XK_Escape;
            case VK_SPACE:
                return XK_space;
            case VK_PAGE_UP:
                return XK_Page_Up;
            case VK_PAGE_DOWN:
                return XK_Page_Down;
            case VK_END:
                return XK_End;
            case VK_HOME:
                return XK_Home;
            case VK_LEFT:
                return XK_Left;
            case VK_UP:
                return XK_Up;
            case VK_RIGHT:
                return XK_Right;
            case VK_DOWN:
                return XK_Down;
            case VK_COMMA:
                return XK_comma;
            case VK_MINUS:
                return XK_minus;
            case VK_PERIOD:
                return XK_period;
            case VK_SLASH:
                return XK_slash;
            case VK_0:
                return XK_0;
            case VK_1:
                return XK_1;
            case VK_2:
                return XK_2;
            case VK_3:
                return XK_3;
            case VK_4:
                return XK_4;
            case VK_5:
                return XK_5;
            case VK_6:
                return XK_6;
            case VK_7:
                return XK_7;
            case VK_8:
                return XK_8;
            case VK_9:
                return XK_9;
            case VK_SEMICOLON:
                return XK_semicolon;
            case VK_EQUALS:
                return XK_equal;
            case VK_A:
                return shift ? XK_A : XK_a;
            case VK_B:
                return shift ? XK_B : XK_b;
            case VK_C:
                return shift ? XK_C : XK_c;
            case VK_D:
                return shift ? XK_D : XK_d;
            case VK_E:
                return shift ? XK_E : XK_e;
            case VK_F:
                return shift ? XK_F : XK_f;
            case VK_G:
                return shift ? XK_G : XK_g;
            case VK_H:
                return shift ? XK_H : XK_h;
            case VK_I:
                return shift ? XK_I : XK_i;
            case VK_J:
                return shift ? XK_J : XK_j;
            case VK_K:
                return shift ? XK_K : XK_k;
            case VK_L:
                return shift ? XK_L : XK_l;
            case VK_M:
                return shift ? XK_M : XK_m;
            case VK_N:
                return shift ? XK_N : XK_n;
            case VK_O:
                return shift ? XK_O : XK_o;
            case VK_P:
                return shift ? XK_P : XK_p;
            case VK_Q:
                return shift ? XK_Q : XK_q;
            case VK_R:
                return shift ? XK_R : XK_r;
            case VK_S:
                return shift ? XK_S : XK_s;
            case VK_T:
                return shift ? XK_T : XK_t;
            case VK_U:
                return shift ? XK_U : XK_u;
            case VK_V:
                return shift ? XK_V : XK_v;
            case VK_W:
                return shift ? XK_W : XK_w;
            case VK_X:
                return shift ? XK_X : XK_x;
            case VK_Y:
                return shift ? XK_Y : XK_y;
            case VK_Z:
                return shift ? XK_Z : XK_z;
            case VK_OPEN_BRACKET:
                return XK_bracketleft;
            case VK_BACK_SLASH:
                return XK_backslash;
            case VK_CLOSE_BRACKET:
                return XK_bracketright;
            case VK_NUMPAD0:
                return XK_KP_0;
            case VK_NUMPAD1:
                return XK_KP_1;
            case VK_NUMPAD2:
                return XK_KP_2;
            case VK_NUMPAD3:
                return XK_KP_3;
            case VK_NUMPAD4:
                return XK_KP_4;
            case VK_NUMPAD5:
                return XK_KP_5;
            case VK_NUMPAD6:
                return XK_KP_6;
            case VK_NUMPAD7:
                return XK_KP_7;
            case VK_NUMPAD8:
                return XK_KP_8;
            case VK_NUMPAD9:
                return XK_KP_9;
            case VK_MULTIPLY:
                return XK_KP_Multiply;
            case VK_ADD:
                return XK_KP_Add;
            case VK_SEPARATOR:
                return XK_KP_Separator;
            case VK_SUBTRACT:
                return XK_KP_Subtract;
            case VK_DECIMAL:
                return XK_KP_Decimal;
            case VK_DIVIDE:
                return XK_KP_Divide;
            case VK_DELETE:
                return XK_KP_Delete;
            case VK_NUM_LOCK:
                return XK_Num_Lock;
            case VK_SCROLL_LOCK:
                return XK_Scroll_Lock;
            case VK_F1:
                return XK_F1;
            case VK_F2:
                return XK_F2;
            case VK_F3:
                return XK_F3;
            case VK_F4:
                return XK_F4;
            case VK_F5:
                return XK_F5;
            case VK_F6:
                return XK_F6;
            case VK_F7:
                return XK_F7;
            case VK_F8:
                return XK_F8;
            case VK_F9:
                return XK_F9;
            case VK_F10:
                return XK_F10;
            case VK_F11:
                return XK_F11;
            case VK_F12:
                return XK_F12;
            case VK_F13:
                return XK_F13;
            case VK_F14:
                return XK_F14;
            case VK_F15:
                return XK_F15;
            case VK_F16:
                return XK_F16;
            case VK_F17:
                return XK_F17;
            case VK_F18:
                return XK_F18;
            case VK_F19:
                return XK_F19;
            case VK_F20:
                return XK_F20;
            case VK_F21:
                return XK_F21;
            case VK_F22:
                return XK_F22;
            case VK_F23:
                return XK_F23;
            case VK_F24:
                return XK_F24;

            case VK_PRINTSCREEN:
                return XK_Print;
            case VK_INSERT:
                return XK_Insert;
            case VK_HELP:
                return XK_Help;
            case VK_META:
                return XK_Meta_L;
            case VK_KP_UP:
                return XK_KP_Up;
            case VK_KP_DOWN:
                return XK_KP_Down;
            case VK_KP_LEFT:
                return XK_KP_Left;
            case VK_KP_RIGHT:
                return XK_KP_Right;
            case VK_DEAD_GRAVE:
                return XK_dead_grave;
            case VK_DEAD_ACUTE:
                return XK_dead_acute;
            case VK_DEAD_CIRCUMFLEX:
                return XK_dead_circumflex;
            case VK_DEAD_TILDE:
                return XK_dead_tilde;
            case VK_DEAD_MACRON:
                return XK_dead_macron;
            case VK_DEAD_BREVE:
                return XK_dead_breve;
            case VK_DEAD_ABOVEDOT:
                return XK_dead_abovedot;
            case VK_DEAD_DIAERESIS:
                return XK_dead_diaeresis;
            case VK_DEAD_ABOVERING:
                return XK_dead_abovering;
            case VK_DEAD_DOUBLEACUTE:
                return XK_dead_doubleacute;
            case VK_DEAD_CARON:
                return XK_dead_caron;
            case VK_DEAD_CEDILLA:
                return XK_dead_cedilla;
            case VK_DEAD_OGONEK:
                return XK_dead_ogonek;
            case VK_DEAD_IOTA:
                return XK_dead_iota;
            case VK_DEAD_VOICED_SOUND:
                return XK_dead_voiced_sound;
            case VK_DEAD_SEMIVOICED_SOUND:
                return XK_dead_semivoiced_sound;
            case VK_AMPERSAND:
                return XK_ampersand;
            case VK_ASTERISK:
                return XK_asterisk;
            case VK_QUOTEDBL:
                return XK_quotedbl;
            case VK_LESS:
                return XK_less;
            case VK_GREATER:
                return XK_greater;
            case VK_BRACELEFT:
                return XK_bracketleft;
            case VK_BRACERIGHT:
                return XK_bracketright;
            case VK_AT:
                return XK_at;
            case VK_COLON:
                return XK_colon;
            case VK_CIRCUMFLEX:
                return XK_acircumflex;
            case VK_DOLLAR:
                return XK_dollar;
            case VK_EURO_SIGN:
                return XK_EuroSign;
            case VK_EXCLAMATION_MARK:
                return XK_exclam;
            case VK_INVERTED_EXCLAMATION_MARK:
                return XK_exclamdown;
            case VK_LEFT_PARENTHESIS:
                return XK_parenleft;
            case VK_NUMBER_SIGN:
                return XK_numbersign;
            case VK_PLUS:
                return XK_plus;
            case VK_RIGHT_PARENTHESIS:
                return XK_parenright;
            case VK_UNDERSCORE:
                return XK_underscore;
            case VK_WINDOWS:
                return XK_Super_L;
            case VK_COMPOSE:
                return XK_Multi_key;
            case VK_ALT_GRAPH:
                return XK_ISO_Level3_Shift;
            case VK_BEGIN:
                return XK_Begin;
        }
        throw new IllegalArgumentException("Cannot type keycode " + code);
    }

    private boolean requiresShift(int xlibKeySym)
    {
        // This is keyboard layout dependent.
        // Encode here is for US layout
        switch (xlibKeySym) {
            case XK_A:
            case XK_B:
            case XK_C:
            case XK_D:
            case XK_E:
            case XK_F:
            case XK_G:
            case XK_H:
            case XK_I:
            case XK_J:
            case XK_K:
            case XK_L:
            case XK_M:
            case XK_N:
            case XK_O:
            case XK_P:
            case XK_Q:
            case XK_R:
            case XK_S:
            case XK_T:
            case XK_U:
            case XK_V:
            case XK_W:
            case XK_X:
            case XK_Y:
            case XK_Z:
            case XK_exclam:
            case XK_at:
            case XK_numbersign:
            case XK_dollar:
            case XK_percent:
            case XK_asciicircum:
            case XK_ampersand:
            case XK_asterisk:
            case XK_parenleft:
            case XK_parenright:
            case XK_underscore:
            case XK_plus:
            case XK_braceleft:
            case XK_braceright:
            case XK_colon:
            case XK_quotedbl:
            case XK_bar:
            case XK_less:
            case XK_greater:
            case XK_question:
            case XK_asciitilde:
            case XK_plusminus:
                return true;
            default:
                return false;
        }

    }

    @Override
    public void mouseMove(int x, int y)
    {
        try {
            screen.getClient().mouseEvent(mouseButtons, x, y);
            mouseX = x;
            mouseY = y;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void mouseDown(int buttons)
    {
        if ((buttons & Mouse.LEFT) != 0) mouseButtons |= VNC_POINTER_EVENT_BUTTON_1;
        if ((buttons & Mouse.MIDDLE) != 0) mouseButtons |= VNC_POINTER_EVENT_BUTTON_2;
        if ((buttons & Mouse.RIGHT) != 0) mouseButtons |= VNC_POINTER_EVENT_BUTTON_3;
        mouseMove(mouseX, mouseY);
    }

    @Override
    public int mouseUp(int buttons)
    {
        if ((buttons & Mouse.LEFT) != 0) mouseButtons &= ~VNC_POINTER_EVENT_BUTTON_1;
        if ((buttons & Mouse.MIDDLE) != 0) mouseButtons &= ~VNC_POINTER_EVENT_BUTTON_2;
        if ((buttons & Mouse.RIGHT) != 0) mouseButtons &= ~VNC_POINTER_EVENT_BUTTON_3;
        mouseMove(mouseX, mouseY);

        int remainingButtons = 0;
        if ((mouseButtons & VNC_POINTER_EVENT_BUTTON_1) != 0) remainingButtons |= Mouse.LEFT;
        if ((mouseButtons & VNC_POINTER_EVENT_BUTTON_2) != 0) remainingButtons |= Mouse.MIDDLE;
        if ((mouseButtons & VNC_POINTER_EVENT_BUTTON_3) != 0) remainingButtons |= Mouse.RIGHT;
        return remainingButtons;
    }

    @Override
    public void mouseReset()
    {
        mouseButtons = 0;
        mouseMove(mouseX, mouseY);
    }

    @Override
    public void clickStarts()
    {

    }

    @Override
    public void clickEnds()
    {

    }

    @Override
    public void smoothMove(Location dest)
    {
        smoothMove(new Location(mouseX, mouseY), dest, (long) (Settings.MoveMouseDelay * 1000L));
    }

    @Override
    public void smoothMove(Location src, Location dest, long duration)
    {
        if (duration <= 0) {
            mouseMove(dest.getX(), dest.getY());
            return;
        }

        float x = src.getX();
        float y = src.getY();
        float dx = dest.getX() - src.getX();
        float dy = dest.getY() - src.getY();

        long start = System.currentTimeMillis();
        long elapsed = 0;
        do {
            float fraction = (float) elapsed / (float) duration;
            mouseMove((int) (x + fraction * dx), (int) (y + fraction * dy));
            delay(autoDelay);
            elapsed = System.currentTimeMillis() - start;
        } while (elapsed < duration);
        mouseMove(dest.x, dest.y);
    }

    @Override
    public void mouseWheel(int wheelAmt)
    {
        if (wheelAmt == Mouse.WHEEL_DOWN) {
            mouseButtons |= VNC_POINTER_EVENT_BUTTON_5;
            mouseMove(mouseX, mouseY);
            mouseButtons &= ~VNC_POINTER_EVENT_BUTTON_5;
            mouseMove(mouseX, mouseY);
        } else if (wheelAmt == Mouse.WHEEL_UP) {
            mouseButtons |= VNC_POINTER_EVENT_BUTTON_4;
            mouseMove(mouseX, mouseY);
            mouseButtons &= ~VNC_POINTER_EVENT_BUTTON_4;
            mouseMove(mouseX, mouseY);
        }
    }

    @Override
    public void waitForIdle()
    {
    }

    @Override
    public void delay(int ms)
    {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // ignored
        }
    }

    @Override
    public void setAutoDelay(int ms)
    {
        autoDelay = ms;
    }

    @Override
    public Color getColorAt(int x, int y)
    {
        ScreenImage image = captureScreen(new Rectangle(x, y, 1, 1));
        return new Color(image.getImage().getRGB(0, 0));
    }

    @Override
    public void cleanup()
    {

    }
}
