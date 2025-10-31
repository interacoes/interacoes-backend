UPDATE metadatavalue mv
SET text_value = REPLACE(
        REPLACE(mv.text_value,
                'http://localhost:4000',
                'https://interacoes.ltap.ifce.edu.br'),
        'https://dspace.dev.interacoes.ltap.ifce.edu.br',
        'https://interacoes.ltap.ifce.edu.br'
                 )
    FROM metadatafieldregistry mfr
WHERE mv.metadata_field_id = mfr.metadata_field_id
  AND mfr.element = 'identifier'
  AND mfr.qualifier = 'uri'
  AND (mv.text_value LIKE '%http://localhost:4000%'
   OR mv.text_value LIKE '%https://dspace.dev.interacoes.ltap.ifce.edu.br%');
