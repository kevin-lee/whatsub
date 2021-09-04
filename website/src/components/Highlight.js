import React from 'react';

const Highlight = ({children, color}) => (
  <span
    style={{
      backgroundColor: color,
      borderRadius: '10px',
      color: '#fff',
      padding: '5px',
    }}>
    {children}
  </span>
);

export default Highlight;
