package test;

import com.codeborne.selenide.Configuration;
import data.DataHelper;
import data.SQLHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeOptions;
import page.LoginPage;

import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.Selenide.open;
import static data.SQLHelper.cleanAuthCodes;
import static data.SQLHelper.cleanDataBases;

public class BankLoginTest {
    LoginPage loginPage;

    @AfterEach
    void tearDown() {
        cleanAuthCodes();
    }

    @AfterAll
    static void tearDownAll() {
        cleanDataBases();
    }

    @BeforeEach
    void setup() {  // нейтрализация проверки паролей при обновлении Google Chrome
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("credentials_enable_service", false);
        prefs.put("password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);
        Configuration.browserCapabilities = options;

        loginPage = open("http://localhost:9999", LoginPage.class);
    }

    @Test
    void shouldTestSuccessfulLogin() { // проверка Happy Path
        var authInfo = DataHelper.getAuthInfoWithTestData(); // получение тестовых логина и пароля
        var verificationPage = loginPage.validLogin(authInfo); // ввода тестовых данных
        verificationPage.verifyVerificationPageVisibility(); // проверка, что страница ввода кода загружена
        var verificationCode = SQLHelper.getVerificationCode(); // получение проверочного кода из БД
        verificationPage.validverify(verificationCode.getCode()); // ввод проверочного кода
    }

    @Test
    void shouldTestLoginWithRandomUser() { // ввод случайно сгенерированных логина и пароля
        var authInfo = DataHelper.generateRandomUser();
        var verificationPage = loginPage.validLogin(authInfo);
        verificationPage.verifyErrorNotification("Ошибка! Неверно указан логин или пароль");
    }

    @Test
    void shouldTestLoginWithIncorrectVerificationCode() { // ввод неверного проверочного кода
        var authInfo = DataHelper.getAuthInfoWithTestData();
        var verificationPage = loginPage.validLogin(authInfo);
        verificationPage.verifyVerificationPageVisibility();
        var verificationCode = DataHelper.generateVerificationCode();
        verificationPage.verify(verificationCode.getCode());
        verificationPage.verifyErrorNotification("Ошибка! Неверно указан код! Попробуйте ещё раз.");
    }

    @Test
    void shouldTestUnsuccessfulLoginWithIncorrectPasswordx3() { //ввод неправильного пароля 3 раза

        var authInfo = DataHelper.generateRandomUser();
        var verificationPage = loginPage.validLogin(authInfo);
        verificationPage.verifyErrorNotification("Ошибка! Неверно указан логин или пароль");

        for (int count = 0; count < 3; count++){
            loginPage.validLogin(DataHelper.generateRandomUser());
        }

        verificationPage.verifyErrorNotification("Превышено максимальное количество попыток авторизации");
    }
}
