package cz.snyll.Sunny.services.datasenders;

import cz.snyll.Sunny.domain.InfoData;

public interface RemoteDataSender {
    void SendData(InfoData infoData);
}