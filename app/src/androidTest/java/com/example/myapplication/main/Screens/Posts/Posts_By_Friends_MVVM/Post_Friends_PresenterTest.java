package com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVVM;

import android.content.Context;
import android.text.TextUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


@RunWith(JUnit4.class)
public class Post_Friends_PresenterTest {

    Context context;
    Post_Friends_Presenter presenter;


    @Before
    public void setUp(){
        //TODO: теперь доступна во всем тесте
        presenter = new Post_Friends_Presenter((Post_List_view) context);
    }

    @After
    public void close(){
        //TODO: обнулять переменные, закрывать запрос в БД и тд
    }

    @Test
    public void presenterCreatedCorrectly() throws Exception {
        assertNotNull(presenter);
    }


    @Rule
    public final Timeout timeout = new Timeout(1000, TimeUnit.MILLISECONDS);


    @Test
    public void checkFiveXThreeAsFifteen() throws Exception {
        int expected = 15;
        int result = presenter.add(3,5);
        assertEquals(expected, result);
    }

    @Test
    public void checkTwentyXFiveAs100() throws Exception {
        int expected2 = 100;
        int result2 = presenter.add(20,5);
        assertEquals(expected2, result2);
    }

}