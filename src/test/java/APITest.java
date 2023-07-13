import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class APITest {
    private static final String API_URL = "http://3.73.86.8:3333";


    @DataProvider(name = "InvalidUserDataProvider")
    public Object[][] invalidUserDataProvider() {
        return new Object[][]{
                // Empty username
                {null, "email@example.com", "password"},
                // Empty email
                {"username", null, "password"},
                // Empty password
                {"username", "email@example.com", null},
                // All fields are empty
                {null, null, null}
        };
    }

    @DataProvider(name = "ValidUserDataProvider")
    public Object[][] validUserDataProvider() {
        UUID uuid = UUID.randomUUID();
        return new Object[][]{
                // Valid unique user
                {"user_" + uuid, "email_" + uuid + "@example.com", "password_" + uuid}
        };
    }



    @Test(dataProvider = "ValidUserDataProvider")
    public void testCreateUserSuccess(String username, String email, String password) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("password", password);

        given().
                contentType(ContentType.JSON).
                body(user).
        when().
                post(API_URL + "/user/create").
        then().
                assertThat().
                statusCode(200).
                body("success", equalTo(true)).
                body("message", equalTo("User Successully created")).
                body("details.username", equalTo(user.get("username"))).
                body("details.email", equalTo(user.get("email")));
    }

    @Test(dataProvider = "InvalidUserDataProvider")
    public void testCreateUserWithoutRequiredFields(String username, String email, String password) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("password", password);

        given().
                contentType(ContentType.JSON).
                body(user).
        when().
                post(API_URL + "/user/create").
        then().
                assertThat().
                statusCode(400).
                body("success", equalTo(false));
    }

    @Test(dataProvider = "ValidUserDataProvider")
    public void testCreateUserWithDuplicateUsername(String username, String email, String password) {
        // First user
        Map<String, Object> user1 = new HashMap<>();
        user1.put("username", username);
        user1.put("email", email);
        user1.put("password", password);

        given().
                contentType(ContentType.JSON).
                body(user1).
        when().
                post(API_URL + "/user/create").
        then().
                assertThat().
                statusCode(200).
                body("success", equalTo(true));

        // Second user with username from first user
        Map<String, Object> user2 = new HashMap<>();
        user2.put("username", user1.get("username"));
        user2.put("email", "email_" + UUID.randomUUID() + "@example.com");
        user2.put("password", "password_" + UUID.randomUUID());

        given().
                contentType(ContentType.JSON).
                body(user2).
        when().
                post(API_URL + "/user/create").
        then().
                assertThat().
                statusCode(400).
                body("success", equalTo(false));
    }

    @Test
    public void testGetAllUsers() {
        Response response = RestAssured
                .given()
                .when()
                .get(API_URL + "/user/get")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .response();

        // Parse response to user list
        List<Map<String, ?>> userList = response.jsonPath().getList("$");

        // Check that every user has correct structure
        for (Map<String, ?> user : userList) {
            assertUserValid(user);
        }
    }


    private void assertUserValid(Map<String, ?> user) {
        Assert.assertTrue(user.containsKey("id"));
        Assert.assertTrue(user.containsKey("username"));
        Assert.assertTrue(user.containsKey("email"));
        Assert.assertTrue(user.containsKey("password"));
        Assert.assertTrue(user.containsKey("created_at"));
        Assert.assertTrue(user.containsKey("updated_at"));

        Assert.assertNotNull(user.get("id"));
        Assert.assertFalse(((String) user.get("username")).isEmpty());
        Assert.assertFalse(((String) user.get("email")).isEmpty());
        Assert.assertFalse(((String) user.get("password")).isEmpty());
        Assert.assertFalse(((String) user.get("created_at")).isEmpty());
        Assert.assertFalse(((String) user.get("updated_at")).isEmpty());
    }


}
