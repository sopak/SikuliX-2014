package edu.unh.iol.dlc;

import com.tigervnc.rfb.UserPasswdGetter;

class BasicUserPasswdGetter implements UserPasswdGetter
{
    private final String password;

    public BasicUserPasswdGetter(String password)
    {
        this.password = password;
    }

    @Override
    public boolean getUserPasswd(StringBuffer user, StringBuffer passwd)
    {
        if (user != null) {
            user.setLength(0);
        }

        if (password != null) {
            passwd.setLength(0);
            passwd.append(password);

            return true;
        } else {
            return false;
        }
    }
}
