select
  TABLE_NAME,COLUMN_NAME,CONSTRAINT_NAME, REFERENCED_TABLE_NAME,REFERENCED_COLUMN_NAME
from INFORMATION_SCHEMA.KEY_COLUMN_USAGE
where
  REFERENCED_TABLE_NAME = '<table>';


INSERT INTO  Organization (organizationId, name, email) VALUES (1, 'LUKIIOT', 'admin@luism.co');
INSERT INTO  Role (name) VALUES ('administrator');
INSERT INTO  Role (name) VALUES ('user');
INSERT INTO  Permission (name, object, permission , roleId) VALUES ('grant_admin_all', 'all', 'all', 1);
INSERT INTO  Permission (name, object, permission , roleId) VALUES ('grant_user_all', 'main', 'all', 2);
INSERT INTO  User (login, password,salt, email, language, organizationId, roleId) VALUES ('admin', 'CqOAL8ZsqiiPUMIdJAluS664yeg=', 'YaD9S+YI+c0=', 'admin@home.com','en', 1, 1);
INSERT INTO  User (login, password,salt, email, language, organizationId, roleId) VALUES ('user', 'CqOAL8ZsqiiPUMIdJAluS664yeg=', 'YaD9S+YI+c0=', 'user@home.com','en', 1, 2);


insert into `Language` (languageId, name, flag) values (1, 'en', 'flags/GB.png');
insert into `Language` (languageId, name, flag) values (2, 'de', 'flags/DE.png');
insert into `Language` (languageId, name, flag) values (3, 'fr', 'flags/FR.png');
insert into `Language` (languageId, name, flag) values (4, 'it', 'flags/IT.png');

INSERT INTO Configuration (version, projectCode, hardware, organizationId) values ('0.0.1', "LKIT" , "CB5", 1);
INSERT INTO Fleet (fleetId, name, configurationId, icon, mapPointer) values (1, "F1" , 1, "icons/default", "icons/map_default");

INSERT INTO Vehicle (vehicleId, vehicleType, vehicleNumber,
                     smsNumber, protocolVersion, timeZone,
                     countryCode, daylightSavingTime, enabled, fleetId)
    values ('12345678901234567890123456789012', 'Tm 232', '111', '9939939933', '1.0.0', 0, 'CH', false , true , 1);


Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (1,'User_Updated',2,'Einstellungen aktualisiert!','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (2,'alarmID',2,'Alarm #','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (3,'OnlineDiagnostics',2,'Online-Diagnose-System','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (4,'WRONG_PASSWORD_TEXT',2,'Falsches Passwort oder Benutzername! Bitte versuchen Sie es erneut','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (5,'LAST_UPDATE',2,'Letzter Status aktualisiert am:','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (6,'Update',2,'Aktualisieren','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (7,'ST_ONLINE',2,'ONLINE','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (8,'ST_OFFLINE',2,'OFFLINE','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (9,'Tank',2,'Kraftstofftank','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (10,'ST_NONE',2,'ST_NONE','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (11,'tagId',2,'Tag #','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (12,'description',2,'Beschreibung','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (13,'Fleet',2,'Flotte','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (14,'bean_not_found',2,'Benutzer nicht gefunden','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (15,'none',2,'-- keiner --','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (16,'all',2,'-- alle --','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (17,'WelcomeTo',2,'Willkommen bei','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (18,'Organization',2,'Unternehmen','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (19,'lastName',2,'Nachname','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (20,'rcvTimeStamp',2,'Zeit','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (21,'Speed',2,'Geschwindigkeit','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (22,'LOGIN_BUTTON',2,'Login','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (23,'USER_NAME_LABEL',2,'Benutzer','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (24,'password',2,'Kennwort','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (25,'unable_to_update_bean',2,'Konnte nicht die Einstellungen zu aktualisieren ...','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (26,'filter',2,'filter','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (27,'User_Not_Found',2,'Benutzer nicht gefunden!','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (28,'EVENT_LIST',2,'Event-Liste','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (29,'firstName',2,'Vorname','',null);

Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (30,'CURRENT_ALARMS',2,'Alarmliste','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (31,'value',2,'Wert','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (32,'USER_NAME_PROMPT',2,'Ihren Benutzernamen (zB. Admin)','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (33,'DRAG_TARGET',2,'Ziehen Sie die Bahn hier!','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (34,'USER_PASSWORD_LABEL',2,'Passwort','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (35,'Type',2,'Fahrzeugtyp','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (36,'PLEASE_LOGIN_TEXT',2,'Zugriff auf das System geben Sie bitte Ihre Anmeldeinformationen','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (37,'Language',2,'Sprache','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (38,'Battery',2,'Batterie','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (39,'timeStamp',2,'Zeitstempel','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (40,'User_Updated',1,'Settings Updated!','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (41,'alarmID',1,'Alarm #','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (42,'OnlineDiagnostics',1,'Online Diagnostics System','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (43,'WRONG_PASSWORD_TEXT',1,'Wrong password or user name! Please try again','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (44,'LAST_UPDATE',1,'Last Status Updated on:','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (45,'Update',1,'Update','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (46,'unable_to_update',1,'bean=Unable to update settings...','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (47,'ST_ONLINE',1,'ONLINE','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (48,'ST_OFFLINE',1,'OFFLINE','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (49,'Tank',1,'Fuel Tank','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (50,'ST_NONE',1,'ST_NONE','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (51,'tagId',1,'Tag #','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (52,'description',1,'Description','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (53,'Fleet',1,'Fleet','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (54,'bean_not_found',1,'User not found','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (55,'none',1,'-- none --','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (56,'all',1,'-- all --','',null);

Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (57,'WelcomeTo',1,'Welcome to','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (58,'Organization',1,'Organization','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (59,'lastName',1,'Last Name','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (60,'rcvTimeStamp',1,'Time','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (61,'Speed',1,'Speed','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (62,'LOGIN_BUTTON',1,'Login','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (63,'USER_NAME_LABEL',1,'User','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (64,'password',1,'Password','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (65,'filter',1,'filter','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (66,'User_Not_Found',1,'User Not Found!','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (67,'EVENT_LIST',1,'Event List','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (68,'firstName',1,'First Name','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (69,'CURRENT_ALARMS',1,'Alarm List','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (70,'value',1,'Value','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (71,'USER_NAME_PROMPT',1,'Your username (eg. admin)','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (72,'DRAG_TARGET',1,'Drag Vehicle Here!','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (73,'USER_PASSWORD_LABEL',1,'Password','', null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (74,'Type',1,'Vehicle Type','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (75,'PLEASE_LOGIN_TEXT',1,'To access the system please provide your credentials','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (76,'Language',1,'Language','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (77,'Battery',1,'Battery','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (78,'timeStamp',1,'TimeStamp','',null);

Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (118,'User_Updated',4,'Impostazioni aggiornato!','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (119,'alarmID',4,'allarme # ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (120,'OnlineDiagnostics',4,'Sistema diagnostico in linea ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (121,'WRONG_PASSWORD_TEXT',4,'Password errata o nome utente! Riprova ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (122,'LAST_UPDATE',4,'Ultimo Stato Aggiornato il: ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (123,'Update',4,'Aggiornamento ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (124,'ST_ONLINE',4,'ONLINE ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (125,'ST_OFFLINE',4,'OFFLINE ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (126,'Tank',4,'Serbatoio carburante ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (127,'ST_NONE',4,'ST_NONE ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (128,'tagId',4,'Day # ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (129,'description',4,'descrizione ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (130,'Fleet',4,'flotta ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (131,'bean_not_found',4,'Utente non trovato ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (132,'none',4,'- Nessuno - ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (133,'all',4,'- Tutti - ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (134,'WelcomeTo',4,'Benvenuti a ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (135,'Organization',4,'affari ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (136,'lastName',4,'cognome ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (137,'rcvTimeStamp',4,'tempo ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (138,'Speed',4,'VelocitÃ  ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (139,'LOGIN_BUTTON',4,'Accedi ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (140,'USER_NAME_LABEL',4,'utente ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (141,'password',4,'password ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (142,'unable_to_update_bean',4,'Impossibile aggiornare le impostazioni ... ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (143,'filter',4,'filtro ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (144,'User_Not_Found',4,'Utente non trovato! ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (145,'EVENT_LIST',4,'Elenco eventi ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (146,'firstName',4,'nome ','',null);

Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (147,'CURRENT_ALARMS',4,'elenco allarmi ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (148,'value',4,'valore ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (149,'USER_NAME_PROMPT',4,'Il tuo nome utente (ad esempio, Admin.) ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (150,'DRAG_TARGET',4,'Trascinare il brano qui! ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (151,'USER_PASSWORD_LABEL',4,'password ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (152,'Type',4,'tipo di veicolo ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (153,'PLEASE_LOGIN_TEXT',4,'Immettere le credenziali per accedere al sistemai ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (154,'Language',4,'lingua ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (155,'Battery',4,'batteria ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (156,'timeStamp',4,'Timestamp ','',null);
Insert into Translation (translationId,textId,languageId,translation,updateBy,updateTime) values (157,'V_CLIENT_CNX',1,'Cl cx ','',null);

Insert into Translation (textId,languageId,translation) values ('CUR_ALARMS',1,'Active Alarms');
Insert into Translation (textId,languageId,translation) values ('ALL_EVENTS',1,'Alarms Table');
Insert into Translation (textId,languageId,translation) values ('CUR_ALARMS',2,'Alarmliste');
Insert into Translation (textId,languageId,translation) values ('ALL_EVENTS',2,'Alarmtabelle');
Insert into Translation (textId,languageId,translation) values ('ALARM_DESCRIPTION',1,'Details');
Insert into Translation (textId,languageId,translation) values ('ALARM_WORKSHOP',1,'Workshop');
Insert into Translation (textId,languageId,translation) values ('HISTORY_ALARMS',1,'History');
Insert into Translation (textId,languageId,translation) values ('ALARM_DESCRIPTION',2,'Einzelheiten');
Insert into Translation (textId,languageId,translation) values ('ALARM_WORKSHOP',2,'Werkstatt');
Insert into Translation (textId,languageId,translation) values ('HISTORY_ALARMS',2,'Vergangenheit');
Insert into Translation (textId,languageId,translation) values ('ALL_ENVIRONMENT',1,'Environment Data');
Insert into Translation (textId,languageId,translation) values ('id',1,'ID');
Insert into Translation (textId,languageId,translation) values ('status',1,'Status');
Insert into Translation (textId,languageId,translation) values ('ack',1,'Acknowledge');
Insert into Translation (textId,languageId,translation) values ('alarmTagHistoryInfoId',1,'Alarm Value ID');
Insert into Translation (textId,languageId,translation) values ('milliSeconds',1,'MilliSeconds');
Insert into Translation (textId,languageId,translation) values ('ALL_ENVIRONMENT',2,'Umweltdaten');
Insert into Translation (textId,languageId,translation) values ('id',2,'ID');
Insert into Translation (textId,languageId,translation) values ('status',2,'Status');
Insert into Translation (textId,languageId,translation) values ('ack',2,'Bestätigen');
Insert into Translation (textId,languageId,translation) values ('alarmTagHistoryInfoId',2,'Alarmwert ID');
Insert into Translation (textId,languageId,translation) values ('milliSeconds',2,'Millisekunden');
Insert into Translation (textId,languageId,translation) values ('CLOSE_ALARM_DETAILS',1,'Close');
Insert into Translation (textId,languageId,translation) values ('GET_ALARM_ENV_DATA',1,'Show Environment Data');
Insert into Translation (textId,languageId,translation) values ('CLOSE_CHARTS',1,'Close Charts');
Insert into Translation (textId,languageId,translation) values ('ALARM_ENV_DATA_NOT_AVAILABLE',1,'Environment Data Not Available');
Insert into Translation (textId,languageId,translation) values ('CLOSE_ALARM_DETAILS',2,'schließen');
Insert into Translation (textId,languageId,translation) values ('GET_ALARM_ENV_DATA',2,'Zeige Umweltdaten');
Insert into Translation (textId,languageId,translation) values ('CLOSE_CHARTS',2,'Schließen Charts');
Insert into Translation (textId,languageId,translation) values ('ALARM_ENV_DATA_NOT_AVAILABLE',2,'Umweltdaten nicht verfügbar');
Insert into Translation (textId,languageId,translation) values ('eventIndex',1,'Event Index');
Insert into Translation (textId,languageId,translation) values ('categoryIndex',1,'Category Index');
Insert into Translation (textId,languageId,translation) values ('timeStampSeconds',1,'TimeStamp Seconds');
Insert into Translation (textId,languageId,translation) values ('timeStampMilliSeconds',1,'TimeStamp Milliseconds');
Insert into Translation (textId,languageId,translation) values ('WRONG_PASS_RECOVERY_CONTACT_ADMIN', 1,'Wrong password details inserted, please contact the system administrator!');
Insert into Translation (textId,languageId,translation) values ('PASSWORDS_DONT_MATCH', 1,'The passwords dont match');
Insert into Translation (textId,languageId,translation) values ('PASSWORD_CHANGED_GO_TO_LOGIN', 1,'Successful password changed, please login with the new password!');
Insert into Translation (textId,languageId,translation) values ('USER_VALIDATE_PASSWORD_LABEL', 1,'Repeat password');
Insert into Translation (textId,languageId,translation) values ('RESET_PASS_BNT', 1,'Reset Password');
Insert into Translation (textId,languageId,translation) values ('PASS_RECOVERY', 1,'Forgot password?');

Insert into Translation (textId,languageId,translation) values ('R_PASS_EMAIL_LABEL', 1,'Email');
Insert into Translation (textId,languageId,translation) values ('R_PASS_EMAIL_PROMPT', 1,'Your registered e-mail address');
Insert into Translation (textId,languageId,translation) values ('R_PASS_LNAME_LABEL', 1,'Last Name');
Insert into Translation (textId,languageId,translation) values ('R_PASS_LNAME_PROMPT', 1,'You last name');
Insert into Translation (textId,languageId,translation) values ('R_PASS_BUTTON', 1,'Reset Password');

Insert into Translation (textId,languageId,translation) values ('WRONG_PASS_RECOVERY_CONTACT_ADMIN', 2,'Falsches Passwort Details eingefügt, wenden Sie sich an den Systemadministrator!');
Insert into Translation (textId,languageId,translation) values ('PASSWORDS_DONT_MATCH', 2,'Die Passwörter nicht übereinstimmen');
Insert into Translation (textId,languageId,translation) values ('PASSWORD_CHANGED_GO_TO_LOGIN', 2, 'Erfolgreiche Passwort geändert, geben Sie hier das neue Passwort!');
Insert into Translation (textId,languageId,translation) values ('USER_VALIDATE_PASSWORD_LABEL', 2,'Passwort wiederholen');
Insert into Translation (textId,languageId,translation) values ('RESET_PASS_BNT', 2,'Kennwort zurücksetzen');
Insert into Translation (textId,languageId,translation) values ('PASS_RECOVERY', 2,'Passwort vergessen?');
Insert into Translation (textId,languageId,translation) values ('R_PASS_EMAIL_LABEL', 2,'E-Mail');
Insert into Translation (textId,languageId,translation) values ('R_PASS_EMAIL_PROMPT', 2,'Ihre registrierte E-Mail-Adresse');
Insert into Translation (textId,languageId,translation) values ('R_PASS_LNAME_LABEL', 2,'Nachname');
Insert into Translation (textId,languageId,translation) values ('R_PASS_LNAME_PROMPT', 2,'Sie Nachnamen');
Insert into Translation (textId,languageId,translation) values ('R_PASS_BUTTON', 2,'Kennwort zurücksetzen');
