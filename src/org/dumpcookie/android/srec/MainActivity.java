/*
 * ---------------------------------------------------------------------------
 * Copyright 2014 wkpark at gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the 'License'); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * ---------------------------------------------------------------------------
 */
package org.dumpcookie.android.srec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.lang.StringBuilder;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import android.speech.srec.Recognizer;
import android.speech.srec.MicrophoneInputStream;

public class MainActivity extends Activity {
    private static final String TAG = "SREC Test";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button start = (Button) findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    test();
                } catch(IOException e) {
                    Log.d(TAG, "Error!!");
                }
            }
        });
    }

    private void test() throws IOException {
        Recognizer recognizer;
        Recognizer.Grammar grammar;

        // create and start audio input
        InputStream audio = new MicrophoneInputStream(11025, 11025 * 5);
        // create a Recognizer
        String cdir = Recognizer.getConfigDir(null);

        File par = new File(cdir, "baseline11k.par");

        // slightly modified to copy asset files
        if (par.exists()) {
            recognizer = new Recognizer(cdir + "/baseline11k.par");
            // create and load a Grammar
            grammar = recognizer.new Grammar(cdir
                    + "/grammars/VoiceDialer.g2g");
        } else {
            // make /mnt/sdcard/Android/data/foobar.../files
            File external = Environment.getExternalStorageDirectory();

            StringBuilder sb = new StringBuilder();
            sb.append(external.getAbsolutePath());
            sb.append("/Android/data");
            sb.append(this.getPackageName());
            sb.append("/files");
            File dir = new File(sb.toString());
            if (!dir.exists()) {
                dir.mkdirs();
            }

            cdir = dir.toString();

            copyAsset(this, "baseline11k.par", cdir);
            copyAsset(this, "VoiceDialer.g2g", cdir);

            recognizer = new Recognizer(cdir + "/baseline11k.par");
            // create and load a Grammar
            grammar = recognizer.new Grammar(cdir
                    + "/VoiceDialer.g2g");
        }

        // setup the Grammar to work with the Recognizer
        grammar.setupRecognizer();
        // fill the Grammar slots with names and save, if required
        grammar.resetAllSlots();
        // for (String name : names) grammar.addWordToSlot("@Names", name, null, 1, "V=1");
        // names can be { "hello", "foobar", "kim" }

        grammar.compile();
        // temporary save the compiled g2g file.
        //grammar.save("/mnt/sdcard/foobar.g2g");
        // start the Recognizer
        recognizer.start();
        // loop over Recognizer events
        while (true) {
            switch (recognizer.advance()) {
                case Recognizer.EVENT_INCOMPLETE:
                case Recognizer.EVENT_STARTED:
                case Recognizer.EVENT_START_OF_VOICING:
                case Recognizer.EVENT_END_OF_VOICING:
                    // let the Recognizer continue to run
                    continue;

                case Recognizer.EVENT_RECOGNITION_RESULT:
                    // success, so fetch results here!
                    String str = "";
                    for (int i = 0; i < recognizer.getResultCount(); i++) {
                        String result = recognizer.getResult(i, Recognizer.KEY_LITERAL);
                        Log.d(TAG, "result " + result);
                        str+= result + "\n";
                    }

                    Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
                    break;

                case Recognizer.EVENT_NEED_MORE_AUDIO:
                    // put more audio in the Recognizer
                    recognizer.putAudio(audio);
                    continue;
                default:
                    // notifyFailure();
                    break;
            }

            break;
        }

        // stop the Recognizer
        recognizer.stop();
        // destroy the Recognizer
        recognizer.destroy();
        // stop the audio device
        audio.close();
    }

    private void copyAsset(Context context, String filename, String dir) {
        // copy default zinnia-model file
        AssetManager asset = context.getAssets();

        File outfile = new File(dir, filename);

        InputStream in = null;
        OutputStream out = null;
        try {
            in = asset.open(filename);
            out = new FileOutputStream(outfile);

            byte[] buffer = new byte[1024];
            int read;
            while((read = in.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }
            in.close();

            in = null;
            out.flush();
            out.close();
            out = null;
        } catch(IOException e) {
            Log.e("tag", "Failed to copy asset file: " + filename, e);
        }
    }
}
