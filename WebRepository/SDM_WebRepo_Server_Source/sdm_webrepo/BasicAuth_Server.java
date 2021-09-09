// 
// Decompiled by Procyon v0.5.36
// 

package sdm_webrepo;

import com.sun.net.httpserver.BasicAuthenticator;

class BasicAuth_Server extends BasicAuthenticator
{
    private final String valid_user;
    private final String valid_pwd;
    
    public BasicAuth_Server(final String realm, final String user, final String pwd) {
        super(realm);
        this.valid_user = user;
        this.valid_pwd = pwd;
    }
    
    @Override
    public boolean checkCredentials(final String userNameInput, final String passwordInput) {
        return userNameInput.equals(this.valid_user) && passwordInput.equals(this.valid_pwd);
    }
}
