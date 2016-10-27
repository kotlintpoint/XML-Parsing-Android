package demo.example.com.xmlparsing.activity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import demo.example.com.xmlparsing.R;
import demo.example.com.xmlparsing.adapter.NewsAdapter;
import demo.example.com.xmlparsing.model.News;

public class MainActivity extends AppCompatActivity {

    String url="http://feeds.feedburner.com/ndtvnews-top-stories?format=xml";
    News news;
    RecyclerView recyclerView;
    ArrayList<News> newsList;
    NewsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        newsList = new ArrayList<>();
        adapter = new NewsAdapter(this, newsList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);

        new FetchData().execute();
    }

    class FetchData extends AsyncTask<String, Void,String>
    {
        ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd=new ProgressDialog(MainActivity.this);
            pd.setMessage("Wait While Loading...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            doXmlParsing();
            return null;
        }

        private void doXmlParsing() {
            SAXParserFactory spf=SAXParserFactory.newInstance();
            try {
                SAXParser parser=spf.newSAXParser();

                DefaultHandler handler=new DefaultHandler()
                {
                    boolean bTitle, bDesc, bLink, bStoryImage;

                    @Override
                    public void startElement(String uri, String localName, String qName,
                                             Attributes attributes) throws SAXException {
                        super.startElement(uri, localName, qName, attributes);
                        if(localName.equals("title"))
                        {
                            news = new News();
                            bTitle=true;
                        }else if(localName.equals("link"))
                        {
                            bLink=true;
                        }else if(localName.equals("description"))
                        {
                            bDesc=true;
                        }
                        else if(localName.equals("StoryImage"))
                        {bStoryImage=true;}
                    }

                    @Override
                    public void endElement(String uri, String localName,
                                           String qName) throws SAXException {
                        super.endElement(uri, localName, qName);
                        if(localName.equals("title"))
                        {
                            bTitle=false;
                        }else if(localName.equals("link"))
                        {
                            bLink=false;
                        }else if(localName.equals("description"))
                        {
                            bDesc=false;
                            newsList.add(news);
                        }else if(localName.equals("StoryImage"))
                        {
                            bStoryImage=false;
                        }
                    }

                    @Override
                    public void characters(char[] ch, int start, int length) throws SAXException {
                        super.characters(ch, start, length);
                        if(bTitle)
                        {
                            news.setTitle(new String(ch,start,length));
                        }else if(bLink)
                        {
                            news.setLink(new String(ch,start,length));
                        }else if(bDesc)
                        {
                            news.setDescription(new String(ch,start,length));
                        }else if(bStoryImage)
                        {
                            try {
                                //String temp=new String(ch,start, length);
                                news.setStoryImage(new String(ch,start,length));
                                URL url = new URL(news.getStoryImage());
                                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                news.setmBitmap(bmp);
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                };

                parser.parse(url,handler);


            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            adapter.notifyDataSetChanged();
        }
    }
}
