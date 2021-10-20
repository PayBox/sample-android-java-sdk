package money.paybox.samplejava;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import money.paybox.payboxsdk.Interfaces.PBListener;
import money.paybox.payboxsdk.Model.Capture;
import money.paybox.payboxsdk.Model.Card;
import money.paybox.payboxsdk.Model.Error;
import money.paybox.payboxsdk.Model.PStatus;
import money.paybox.payboxsdk.Model.RecurringPaid;
import money.paybox.payboxsdk.Model.Response;
import money.paybox.payboxsdk.PBHelper;
import money.paybox.payboxsdk.Utils.Constants;

public class MainActivity extends AppCompatActivity implements PBListener {
    private View loaderView;
    private TextView outputTextView;

    //Необходимо заменить тестовый secretKey и merchantId на свой
    private final String secretKey = "UnPLLvWsuXPyC3wd";
    private final int merchantId = 503623;

    //Если email или phone не указан, то выбор будет предложен на сайте платежного гейта
    private final String email = "user@mail.com";
    private final String phone = "77012345678";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loaderView = findViewById(R.id.loaderView);
        outputTextView = findViewById(R.id.outputTextView);

        //Инициализация нового платежа
        findViewById(R.id.buttonInitPayment).setOnClickListener(v -> {
            outputTextView.setText("");
            loaderView.setVisibility(View.VISIBLE);

            //Параметр указывающий на рекурентность платежа
            boolean checkIsRecurring = false;

            String payUserId = "1234";
            float payAmount = 10;
            String payComment = "some description";

            if(checkIsRecurring){
                //При активации рекурентного платежа указывается период от 1 до 156 месяцев
                PBHelper.getSdk().enableRecurring(2);
            } else {
                PBHelper.getSdk().disableRecurring();
            }

            PBHelper.getSdk().initNewPayment(null, payUserId, payAmount, payComment, null);
        });

        //Отображение списка привязанных карт
        findViewById(R.id.buttonShowCards).setOnClickListener(v -> {
            outputTextView.setText("");
            loaderView.setVisibility(View.VISIBLE);

            String userId = "1234";

            PBHelper.getSdk().getCards(userId);
        });

        //Привязка новой карты
        findViewById(R.id.buttonAddCard).setOnClickListener(v -> {
            outputTextView.setText("");
            loaderView.setVisibility(View.VISIBLE);

            String userId = "1234";

            //postUrl - для обратной связи
            String postUrl = "http://test.paybox.kz/";

            PBHelper.getSdk().addCard(userId, postUrl);
        });

        //Удаление привязанной карты по ID
        findViewById(R.id.buttonDeleteCard).setOnClickListener(v -> {
            String userId = "1234";

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText edittext = new EditText(this);
            edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
            alert.setMessage("Введите card ID");
            alert.setTitle("Удаление карты");

            alert.setView(edittext);

            alert.setPositiveButton("Удалить", (dialog, whichButton) -> {
                try {
                    int cardId = Integer.parseInt(edittext.getText().toString());

                    outputTextView.setText("");
                    loaderView.setVisibility(View.VISIBLE);

                    PBHelper.getSdk().removeCard(userId, cardId);
                } catch (Exception e) {
                    //integer required
                }
            });

            alert.setNegativeButton("Отмена", (dialog, whichButton) -> {});

            alert.show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        loaderView.setVisibility(View.GONE);

        //Вызов инициализации SDK
        App.instance.initBuilder(secretKey, merchantId, email, phone);
        App.instance.builder.build();

        //Регистрация текущего активити для просушивания событий
        PBHelper.getSdk().registerPbListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Отвязываем текущее активити от просушивания событий
        PBHelper.getSdk().removePbListener(this);
    }

    @Override
    public void onCardList(ArrayList<Card> cards) {
        loaderView.setVisibility(View.GONE);

        if(cards.isEmpty()){
            outputTextView.setText("");
        }
        String message = new String();
        for(Card card : cards){
            message += "Card hash = " + card.getCardhash() + "\n" +
                    "Card ID = " + card.getCardId() + "\n" +
                    "Recurring profile = " + card.getRecurringProfile() + "\n" +
                    "Created At = " + card.getDate() + "\n" +
                    "Status = " + card.getStatus() + "\n\n";

        }
        outputTextView.setText(message);
    }

    @Override
    public void onPaymentRevoke(Response response) {
        loaderView.setVisibility(View.GONE);
        outputTextView.setText("Status = "+ response.getStatus());
    }

    @Override
    public void onPaymentPaid(Response response) {
        loaderView.setVisibility(View.GONE);
        outputTextView.setText("Payment ID = " + response.getPaymentId() +
                "\nStatus = " + response.getStatus());
    }

    @Override
    public void onPaymentStatus(PStatus pStatus) {
        loaderView.setVisibility(View.GONE);
        outputTextView.setText("Status = " + pStatus.getStatus() +
                "\nPayment system = " + pStatus.getPaymentSystem() +
                "\nTransaction Status = " + pStatus.getTransactionStatus() +
                "\nCaptured = " + pStatus.isCaptured() +
                "\nCan reject = " + pStatus.isCanReject() +
                "\nCard pan = " + pStatus.getCardPan());
    }

    @Override
    public void onCardAdded(Response response) {
        loaderView.setVisibility(View.GONE);
        outputTextView.setText("Payment ID = " + response.getPaymentId() +
                "\nStatus = " + response.getStatus());
    }

    @Override
    public void onCardRemoved(Card card) {
        loaderView.setVisibility(View.GONE);
        if(card != null) {
            outputTextView.setText("\nDeleted At = " + card.getDate() +
                    "\nStatus = " + card.getStatus());
        } else {
            outputTextView.setText("");
        }
    }

    @Override
    public void onCardPayInited(Response response) {
        loaderView.setVisibility(View.GONE);
        outputTextView.setText("Status = " + response.getStatus() +
                "\nPayment ID = " + response.getPaymentId());
        PBHelper.getSdk().payWithCard(Integer.parseInt(response.getPaymentId()));
    }

    @Override
    public void onCardPaid(Response response) {
        loaderView.setVisibility(View.GONE);
        outputTextView.setText("Payment ID = " + response.getPaymentId() +
                "\nStatus = " + response.getStatus());
    }

    @Override
    public void onRecurringPaid(RecurringPaid recurringPaid) {
        loaderView.setVisibility(View.GONE);
        Constants.logMessage("Rec paid");
        outputTextView.setText("Payment ID = " + recurringPaid.getPaymentId() +
                "\nStatus = " + recurringPaid.getStatus() +
                "\nCurrency = " + recurringPaid.getCurrency() +
                "\nDate = " + recurringPaid.getExpireDate().toGMTString());
    }

    @Override
    public void onPaymentCaptured(Capture capture) {
        loaderView.setVisibility(View.GONE);
        outputTextView.setText("Status = " + capture.getStatus() +
                "\nAmount = " + capture.getAmount() +
                "\nClearing Amount = " + capture.getClearingAmount());
    }

    @Override
    public void onPaymentCanceled(Response response) {
        loaderView.setVisibility(View.GONE);
        outputTextView.setText("Status = " + response.getStatus());
    }

    @Override
    public void onError(Error error) {
        loaderView.setVisibility(View.GONE);
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),error.getErrorDesription(),Snackbar.LENGTH_INDEFINITE);
        snackbar.setDuration(5000);
        snackbar.show();
    }
}