// 
// Decompiled by Procyon v0.5.36
// 

package sdm_webrepo;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface IRepoWS
{
    @WebMethod
    String[] ListCredentials();
    
    @WebMethod
    String[] ImportRecord(final String p0);
    
    @WebMethod
    String ExportRecord(final String p0, final String p1, final String p2);
}
