INSERT INTO `uniqueid` (`id`, `hash`, `uuid`, `mem_SerialNumber`, `deviceID`, `manufacturer`, `name`, `processorId`, `SMBIOSBIOSVersion`, `serialNumber`, `volumeSerialNumber`) VALUES
    ('1', '3414236816ff1ddaebd74fc8af44d940', '4C4C4544-0037-4410-8031-B4C04F44354A', '3378E4EB', 'PCIIDE\\IDECHANNEL\\4&3486C163&0&0', 'Dell Inc.', 'Intel(R) Xeon(R) CPU E5-2665 0 @ 2.40GHz', 'BFEBFBFF000206D7', 'A03', '47D1D5J', 'F292DC09'),
    ('2', '5f82ce23c6cff8231c4ac85dd9d52fe5', '6D64F604-07EC-E111-9835-B888E3A02CF2', '1B1A2DE9', 'PCI\\VEN_8086&DEV_1E03&SUBSYS_06471025&REV_04\\3&11583659&0&FA', 'Acer', 'Intel(R) Core(TM) i5-3210M CPU @ 2.50GHz', 'BFEBFBFF000306A9', 'V2.02', 'NXRZLEC00723701BB33400', '56B6127C');

INSERT INTO `unique_id_users` (`user_id`, `uniqueid_hash`) VALUES
    ('1', '3414236816ff1ddaebd74fc8af44d940'),
    ('5', '5f82ce23c6cff8231c4ac85dd9d52fe5');
