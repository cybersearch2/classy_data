package au.com.cybersearch2.robolectric;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FsFile;

/**
 * Create Manifest which gets libraries from location "target/unpacked-libs"
 * MavenAndroidManifest
 * @author Andrew Bowley
 * 15 Apr 2015
 */
public class MavenAndroidManifest extends AndroidManifest 
{
    public MavenAndroidManifest(FsFile baseDir) 
    {
        super(baseDir.join("AndroidManifest.xml"), baseDir.join("res"), baseDir.join("assets"));
    }

    @Override 
    protected List<FsFile> findLibraries() 
    {
        // Try unpack folder from maven.
        FsFile unpack = getBaseDir().join("target/unpacked-libs");
        if (unpack.exists()) 
        {
            FsFile[] libs = unpack.listFiles();
            if (libs != null) 
              return Arrays.asList(libs);
        }
        return Collections.emptyList();
    }

    @Override 
    protected AndroidManifest createLibraryAndroidManifest(FsFile libraryBaseDir) 
    {
        return new MavenAndroidManifest(libraryBaseDir);
    }
}