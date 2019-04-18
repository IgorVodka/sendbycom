package iu5.sendbycom.application;

import iu5.sendbycom.application.datatypes.SwapRolesResult;
import iu5.sendbycom.physical.exception.DataTooBigException;

public interface Swappable {
    public void requestSwapRoles() throws DataTooBigException;
    public void respondSwapRoles(SwapRolesResult result) throws DataTooBigException;
}
