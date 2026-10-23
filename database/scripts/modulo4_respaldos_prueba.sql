USE master;
GO

DECLARE @ruta NVARCHAR(260);
EXEC master.dbo.xp_instance_regread
    N'HKEY_LOCAL_MACHINE',
    N'Software\Microsoft\MSSQLServer\MSSQLServer',
    N'BackupDirectory',
    @ruta OUTPUT;

IF @ruta IS NULL
BEGIN
    RAISERROR('No se pudo obtener la carpeta de respaldos de la instancia.', 16, 1);
    RETURN;
END

IF RIGHT(@ruta, 1) <> '\'
    SET @ruta = @ruta + '\';

DECLARE @marca NVARCHAR(20) = FORMAT(SYSDATETIME(), 'yyyyMMdd_HHmmss');
DECLARE @archivo NVARCHAR(400);
DECLARE @sql NVARCHAR(MAX);

SET @archivo = REPLACE(@ruta + N'DB_ProyectoGrupo4_FULL_' + @marca + N'.bak', '''', '''''');
SET @sql = N'BACKUP DATABASE DB_ProyectoGrupo4 TO DISK = N''' + @archivo
         + N''' WITH INIT, CHECKSUM, NAME = N''Completo DB_ProyectoGrupo4'';';
EXEC sp_executesql @sql;

SET @archivo = REPLACE(@ruta + N'DB_ProyectoGrupo4_DIFF_' + @marca + N'.bak', '''', '''''');
SET @sql = N'BACKUP DATABASE DB_ProyectoGrupo4 TO DISK = N''' + @archivo
         + N''' WITH DIFFERENTIAL, INIT, CHECKSUM, NAME = N''Diferencial DB_ProyectoGrupo4'';';
EXEC sp_executesql @sql;

IF EXISTS (SELECT 1 FROM sys.databases
           WHERE name = N'DB_ProyectoGrupo4' AND recovery_model_desc = N'FULL')
BEGIN
    SET @archivo = REPLACE(@ruta + N'DB_ProyectoGrupo4_LOG_' + @marca + N'.trn', '''', '''''');
    SET @sql = N'BACKUP LOG DB_ProyectoGrupo4 TO DISK = N''' + @archivo
             + N''' WITH INIT, CHECKSUM, NAME = N''Log DB_ProyectoGrupo4'';';
EXEC sp_executesql @sql;
END
GO