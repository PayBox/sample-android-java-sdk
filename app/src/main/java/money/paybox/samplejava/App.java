package money.paybox.samplejava;

import android.app.Application;

import money.paybox.payboxsdk.PBHelper;
import money.paybox.payboxsdk.Utils.Constants;

public class App extends Application {
    private final String checkUrl = "http://test.paybox.kz/";
    private final String resultUrl = "http://test.paybox.kz/";
    private final String refundUrl = "http://test.paybox.kz/";
    private final String captureUrl = "http://test.paybox.kz/";

    public static App instance;
    public PBHelper.Builder builder;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public void initBuilder(String secretKey, int merchantId, String email, String phone) {
        //Инициализация SDK
        builder = new PBHelper.Builder(this, secretKey, merchantId)
                //Выбор платежной системы
                .setPaymentSystem(Constants.PBPAYMENT_SYSTEM.EPAYWEBKZT)
                //Выбор валюты платежа
                .setPaymentCurrency(Constants.CURRENCY.KZT)
                //Дополнительная информация пользователя
                .setUserInfo(email, phone)
                //Активация автоклиринга
                .enabledAutoClearing(true)
                //Для активации режима тестирования
                .enabledTestMode(true)
                //Для передачи информации от платежного гейта
                .setFeedBackUrl(checkUrl, resultUrl, refundUrl, captureUrl, Constants.PBREQUEST_METHOD.GET)
                //Время от 300 до 604800 (в секундах) в течение которого платеж должен быть завершен
                .setPaymentLifeTime(300);
    }
}
