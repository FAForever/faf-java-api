package com.faforever.api.map;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.Map;
import com.faforever.api.content.ContentService;
import com.faforever.api.data.domain.MapVersion;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ErrorCode;
import com.google.common.io.ByteStreams;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

import static com.faforever.api.error.ApiExceptionWithCode.apiExceptionWithCode;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MapServiceTest {
  private MapService instance;

  @Rule
  public TemporaryFolder temporaryDirectory = new TemporaryFolder();
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private MapRepository mapRepository;
  @Mock
  private FafApiProperties fafApiProperties;
  @Mock
  private ContentService contentService;


  @Before
  public void setUp() {
    instance = new MapService(fafApiProperties, mapRepository, contentService);
    when(fafApiProperties.getMap()).thenReturn(new Map()
        .setFinalDirectory(temporaryDirectory.getRoot().getAbsolutePath()));
    when(contentService.createTempDir()).thenReturn(temporaryDirectory.getRoot().toPath());
  }

  @After
  public void shutDown() {
    if (Files.exists(temporaryDirectory.getRoot().toPath())) {
      FileSystemUtils.deleteRecursively(temporaryDirectory.getRoot());
    }
  }

// FIXME
//  @Test
//  public void zipFileanameAllreadyExists() throws IOException {
//    String name = "myCollMap.zip";
//    Path conflicted = Paths.get(temporaryDirectory.getRoot().getAbsolutePath(), name);
//    conflicted.toFile().createNewFile();
//    expectedException.expect(apiExceptionWithCode(ErrorCode.MAP_NAME_CONFLICT));
//    instance.uploadMap(null, name, null);
//  }

  @Test
  public void emptyZip() throws IOException {
    String zipFilename = "empty.zip";
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);
      expectedException.expect(apiExceptionWithCode(ErrorCode.MAP_MISSING_MAP_FOLDER_INSIDE_ZIP));
      instance.uploadMap(mapData, zipFilename, null);
    }
  }

  @Test
  public void notCorrectAuthor() throws IOException {
    String zipFilename = "scmp_037.zip";

    Player me = new Player();
    me.setId(1);
    Player bob = new Player();
    bob.setId(2);

    com.faforever.api.data.domain.Map map = new com.faforever.api.data.domain.Map().setAuthor(bob);
    when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.of(map));
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);
      expectedException.expect(apiExceptionWithCode(ErrorCode.MAP_NOT_ORIGINAL_AUTHOR));
      instance.uploadMap(mapData, zipFilename, me);
    }
  }

  @Test
  public void versionExistsAlready() throws IOException {
    String zipFilename = "scmp_037.zip";

    Player me = new Player();
    me.setId(1);

    com.faforever.api.data.domain.Map map = new com.faforever.api.data.domain.Map()
        .setAuthor(me)
        .setVersions(Arrays.asList(new MapVersion().setVersion(1)));

    when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.of(map));
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);
      expectedException.expect(apiExceptionWithCode(ErrorCode.MAP_VERSION_EXISTS));
      instance.uploadMap(mapData, zipFilename, me);
    }
  }

  @Test
  public void positiveUploadTest() throws IOException {
    String zipFilename = "scmp_037.zip";
    when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.empty());
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);
      Path tmp = temporaryDirectory.getRoot().toPath();
      instance.uploadMap(mapData, zipFilename, null);

      assertFalse(Files.exists(tmp));
    }
  }

  private InputStream loadMapResourceAsStream(String filename) {
    return MapServiceTest.class.getResourceAsStream("/maps/" + filename);
  }

//  public void test_maps_upload_is_metadata_missing(Object oauth, Object app, String tmpdir) {
//    temporaryDirectory.getRoot()
//    upload_dir = tmpdir.mkdir("map_upload")
//    app.config.getMAP_UPLOAD_PATH() = upload_dir.strpath
//    response = oauth.post('/maps/upload',
//        data = {'file':(BytesIO('my file contents'.encode('utf-8')), 'map_name.zip')})
//
//    assertThat(response.status_code == 400
//        assertThat(response.content_type == 'application/vnd.api+json'
//
//            result = json.loads(response.get_data(as_text = True))
//            assertThat(len(result.getErrors()) == 1
//                assertThat(result.getErrors()[0].getCode() == ErrorCode.PARAMETER_MISSING.value.getCode()
//
//  }
//
//  public void test_maps_upload_no_file_results_400(Object oauth, Object app, String tmpdir) {
//    response = oauth.post('/maps/upload', data = {'metadata':'{}'})
//
//    assertThat(response.status_code == 400
//        result = json.loads(response.get_data(as_text = True))
//        assertThat(len(result.getErrors()) == 1
//            assertThat(result.getErrors()[0].getCode() == ErrorCode.UPLOAD_FILE_MISSING.value.getCode()
//  }
//
//
//  public void test_maps_upload_txt_results_400(Object oauth, Object app, String tmpdir) {
//    response = oauth.post('/maps/upload', data = {'file':(BytesIO('1'.encode('utf-8')), 'map_name.txt'),
//    'metadata':json.dumps(dict(is_ranked = True))})
//
//    assertThat(response.status_code == 400
//        result = json.loads(response.get_data(as_text = True))
//        assertThat(len(result.getErrors()) == 1
//            assertThat(result.getErrors()[0].getCode() == ErrorCode.UPLOAD_INVALID_FILE_EXTENSION.value.getCode()
//  }
//
//  public void test_map_by_name(test_client, Object app, maps)
//
//  :
//  response=test_client.get('/maps?filter%5Bfolder_name%5D=scmp_002.v0001')
//
//  assertThat(response.status_code==200
//      assertThat(response.content_type=='application/vnd.api+json'
//
//      result=json.loads(response.data.decode('utf-8')
//
//  )
//
//  assertThat(len(result)
//
//  ==1
//
//  assertThat(result.getData()[0].getId()=='2'
//      assertThat(result.getData()[0].getAttributes().getAuthor()=='User 2'
//      assertThat(result.getData()[0].getAttributes().getDisplay_name()=='SCMP_002'
//      assertThat(result.getData()[0].getAttributes()[
//      'download_url']=='http://content.faforever.com/faf/vault/maps/scmp_002.v0001.zip'
//      assertThat(result.getData()[0].getAttributes().getThumbnail_url_small()=='http://content.faforever.com/faf/vault' \
//      '/map_previews/small/scmp_002.v0001.png'
//      assertThat(result.getData()[0].getAttributes().getThumbnail_url_large()=='http://content.faforever.com/faf/vault' \
//      '/map_previews/large/scmp_002.v0001.png'
//
//}
//
//  @pytest.mark.parametrize("ranked", [True, False])
//  public void test_map_upload(Object oauth, ranked, maps, upload_dir, preview_dir) {
//    map_zip = os.path.join(os.path.dirname(os.path.realpath(__file__)), '../data/scmp 037.zip')
//    with open (map_zip, 'rb')as file:
//    response = oauth.post('/maps/upload',
//        data = {'file':(file, os.path.basename(map_zip)),
//    'metadata':json.dumps(dict(is_ranked = ranked))})
//
//    assertThat(response.status_code == 200
//        assertThat('ok' == response.get_data(as_text = True)
//            assertThat(os.path.isfile(upload_dir.join(os.path.basename('sludge_test.v0001.zip')).strpath)
//                assertThat(os.path.isfile(preview_dir.join('small', 'sludge_test.v0001.png').strpath)
//                    assertThat(os.path.isfile(preview_dir.join('large', 'sludge_test.v0001.png').strpath)
//
//                        with db.connection:
//    cursor = db.connection.cursor(DictCursor)
//    cursor.execute("SELECT display_name, map_type, battle_type, author from map WHERE id = 5")
//    result = cursor.fetchone()
//
//    assertThat(result.getDisplay_name() == 'Sludge Test'
//        assertThat(result.getMap_type() == 'skirmish'
//            assertThat(result.getBattle_type() == 'FFA'
//                assertThat(result.getAuthor() == 1
//
//                    cursor.execute("SELECT description, max_players, width, height, version, filename, ranked, hidden, map_id "
//                        "from map_version WHERE id = 5")
//                    result = cursor.fetchone()
//
//                    assertThat("The thick, brackish water clings"in result.getDescription()
//                        assertThat(result.getMax_players() == 3
//                            assertThat(result.getWidth() == 256
//                                assertThat(result.getHeight() == 256
//                                    assertThat(result.getVersion() == 1
//                                        assertThat(result.getFilename() == 'maps/sludge_test.v0001.zip'
//                                            assertThat(result.getRanked() == (1 if ranked else 0)
//    assertThat(result.getHidden() == 0
//        assertThat(result.getMap_id() == 5
//  }
//
//  public void test_upload_existing_map(Object oauth, maps, upload_dir, preview_dir) {
//    map_zip = os.path.join(os.path.dirname(os.path.realpath(__file__)), '../data/scmp 037.zip')
//    with open (map_zip, 'rb')as file:
//    oauth.post('/maps/upload',
//        data = {'file':(file, os.path.basename(map_zip)),
//    'metadata':json.dumps(dict(is_ranked = True))})
//
//    with open (map_zip, 'rb')as file:
//    response = oauth.post('/maps/upload',
//        data = {'file':(file, os.path.basename(map_zip)),
//    'metadata':json.dumps(dict(is_ranked = True))})
//
//    result = json.loads(response.data.decode('utf-8'))
//
//    assertThat(response.status_code == 400
//        assertThat(result.getErrors()[0].getCode() == ErrorCode.MAP_VERSION_EXISTS.value.getCode()
//            assertThat(result.getErrors()[0].getMeta().getArgs() ==['Sludge Test', 1]
//
//  }
//
//  public void test_upload_map_with_name_clash(Object oauth, maps, upload_dir, preview_dir) {
//    map_zip = os.path.join(os.path.dirname(os.path.realpath(__file__)), '../data/scmp_037.zip')
//    with open (map_zip, 'rb')as file:
//    oauth.post('/maps/upload',
//        data = {'file':(file, os.path.basename(map_zip)),
//    'metadata':json.dumps(dict(is_ranked = True))})
//
//    map_zip = os.path.join(os.path.dirname(os.path.realpath(__file__)), '../data/scmp 037.zip')
//    with open (map_zip, 'rb')as file:
//    response = oauth.post('/maps/upload',
//        data = {'file':(file, os.path.basename(map_zip)),
//    'metadata':json.dumps(dict(is_ranked = True))})
//
//    result = json.loads(response.data.decode('utf-8'))
//
//    assertThat(response.status_code == 400
//        assertThat(result.getErrors()[0].getCode() == ErrorCode.MAP_NAME_CONFLICT.value.getCode()
//            assertThat(result.getErrors()[0].getMeta().getArgs() ==.getSludge_test.v0001.zip()
//
//  }
//
//  public void test_upload_map_with_invalid_scenario(Object oauth, maps, upload_dir, preview_dir) {
//    map_zip = os.path.join(os.path.dirname(os.path.realpath(__file__)), '../data/invalid_scenario.zip')
//    with open (map_zip, 'rb')as file:
//    response = oauth.post('/maps/upload',
//        data = {'file':(file, os.path.basename(map_zip)),
//    'metadata':json.dumps(dict(is_ranked = True))})
//
//    result = json.loads(response.data.decode('utf-8'))
//
//    assertThat(response.status_code == 400
//        assertThat(len(result.getErrors()) == 6
//            assertThat(result.getErrors()[0].getCode() == 109
//                assertThat(result.getErrors()[1].getCode() == 110
//                    assertThat(result.getErrors()[2].getCode() == 111
//                        assertThat(result.getErrors()[3].getCode() == 112
//                            assertThat(result.getErrors()[4].getCode() == 113
//                                assertThat(result.getErrors()[5].getCode() == 114
//
//  }
//
//  public void test_ladder_maps(test_client, maps):
//    response=test_client.get('maps/ladder1v1')
//
//    assertThat(response.status_code==200
//    assertThat(response.content_type=='application/vnd.api+json'
//
//    result=json.loads(response.data.decode('utf-8'))
//    assertThat('data'in result
//    assertThat(len(result.getData())==1
//    assertThat('type'in result.getData()[0]
//    assertThat(result.getData()[0].getAttributes().getId()=='1'
//    }
//
//public void test_get_map(test_client,maps):
//    response=test_client.get('maps/1')
//
//    assertThat(response.status_code==200
//    assertThat(response.content_type=='application/vnd.api+json'
//
//    result=json.loads(response.data.decode('utf-8'))
//    assertThat(result=={
//    'data':{
//    'attributes':{
//    'folder_name':'scmp_001.v0001',
//    'max_players':4,
//    'id':'1',
//    'thumbnail_url_small':'http://content.faforever.com/faf/vault/map_previews/small/scmp_001.v0001.png',
//    'description':'SCMP 001',
//    'num_draws':0,
//    'times_played':0,
//    'height':5,
//    'thumbnail_url_large':'http://content.faforever.com/faf/vault/map_previews/large/scmp_001.v0001.png',
//    'map_type':'FFA',
//    'version':1,
//    'rating':None,
//    'display_name':'SCMP_001',
//    'battle_type':'skirmish',
//    'width':5,
//    'downloads':0,
//    'create_time':result.getData().getAttributes().getCreate_time(),
//    'author':'User 1',
//    'download_url':'http://content.faforever.com/faf/vault/maps/scmp_001.v0001.zip'
//    },
//    'id':'1',
//    'type':'map'
//    }
//    }
//    }
}
