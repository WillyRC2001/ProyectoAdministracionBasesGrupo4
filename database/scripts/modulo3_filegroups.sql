USE DB_ProyectoGrupo4;
GO

IF NOT EXISTS (SELECT 1 FROM sys.filegroups WHERE name = 'FG_DATOS')
    ALTER DATABASE DB_ProyectoGrupo4 ADD FILEGROUP FG_DATOS;
GO

IF NOT EXISTS (SELECT 1 FROM sys.database_files WHERE name = 'DB_ProyectoGrupo4_datos1')
BEGIN
    DECLARE @ruta NVARCHAR(260) = CAST(SERVERPROPERTY('InstanceDefaultDataPath') AS NVARCHAR(260));
    DECLARE @sql NVARCHAR(MAX) = N'ALTER DATABASE DB_ProyectoGrupo4 ADD FILE (
        NAME = N''DB_ProyectoGrupo4_datos1'',
        FILENAME = N''' + @ruta + N'DB_ProyectoGrupo4_datos1.ndf'',
        SIZE = 16MB,
        FILEGROWTH = 16MB) TO FILEGROUP FG_DATOS;';
EXEC sp_executesql @sql;
END
GO