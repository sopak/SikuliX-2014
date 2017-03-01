package edu.unh.iol.dlc;

import com.tigervnc.rfb.CConnection;
import com.tigervnc.rfb.CSecurity;
import com.tigervnc.rfb.SecurityClient;
import com.tigervnc.rfb.UserPasswdGetter;
import com.tigervnc.vncviewer.CConn;

import java.lang.reflect.Field;

class ThreadLocalSecurityClient extends SecurityClient
{
    private static final ThreadLocal<UserPasswdGetter> UPG = new ThreadLocal<>();
    static {
        CConn.upg = new UserPasswdGetter()
        {
            @Override
            public boolean getUserPasswd(StringBuffer user, StringBuffer pass)
            {
                UserPasswdGetter upg = UPG.get();
                if (upg != null) {
                    return upg.getUserPasswd(user, pass);
                } else {
                    if (user != null) {
                        user.setLength(0);
                    }
                    if (pass != null) {
                        pass.setLength(0);
                    }
                    return false;
                }
            }
        };
    }

    private final UserPasswdGetter upg;

    ThreadLocalSecurityClient(UserPasswdGetter userPasswdGetter)
    {
        upg = userPasswdGetter;
        try {
            Field field = SecurityClient.class.getDeclaredField("msg");
            field.setAccessible(true);
            field.set(this, "");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CSecurity GetCSecurity(int securityType)
    {
        final CSecurity security = super.GetCSecurity(securityType);
        if (security != null) {
            return new CSecurity()
            {
                @Override
                public boolean processMsg(CConnection cConnection)
                {
                    UPG.set(upg);
                    return security.processMsg(cConnection);
                }

                @Override
                public int getType()
                {
                    return security.getType();
                }

                @Override
                public String description()
                {
                    return security.description();
                }
            };
        } else {
            return null;
        }
    }
}
