package au.com.cybersearch2.robolectric;

import java.io.File;

import org.junit.runners.model.InitializationError;
import org.robolectric.AndroidManifest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;

public class ClassyTestRunner extends RobolectricTestRunner 
{

    public ClassyTestRunner(Class<?> testClass) throws InitializationError 
    {
        super(testClass);
    }

    @Override protected AndroidManifest createAppManifest(FsFile manifestFile, FsFile resDir, FsFile assetsDir) 
    {
        return new MavenAndroidManifest(Fs.newFile(new File(".")));
    }
}