USE msdb;
GO

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'proyecto_grupo4')
    CREATE USER proyecto_grupo4 FOR LOGIN proyecto_grupo4;
GO

GRANT SELECT ON dbo.backupset TO proyecto_grupo4;
GRANT SELECT ON dbo.backupmediafamily TO proyecto_grupo4;
GO