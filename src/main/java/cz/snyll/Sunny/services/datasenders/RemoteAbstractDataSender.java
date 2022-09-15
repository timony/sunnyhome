package cz.snyll.Sunny.services.datasenders;

import cz.snyll.Sunny.domain.InfoData;

public abstract class RemoteAbstractDataSender implements RemoteDataSender {
    public abstract void SendData(InfoData infoData);


}
